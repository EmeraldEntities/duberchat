package duberchat.handlers.server;

import duberchat.events.ChannelRemoveMemberEvent;
import duberchat.events.RequestFailedEvent;
import duberchat.events.SerializableEvent;
import duberchat.handlers.Handleable;
import duberchat.server.ChatServer;

import java.io.*;
import java.util.HashMap;

import duberchat.chatutil.Channel;
import duberchat.chatutil.User;

public class ServerChannelRemoveMemberHandler implements Handleable {
  private ChatServer server;

  public ServerChannelRemoveMemberHandler(ChatServer server) {
    this.server = server;
  }

  public void handleEvent(SerializableEvent newEvent) {
    ChannelRemoveMemberEvent event = (ChannelRemoveMemberEvent) newEvent;
    Channel toDeleteFrom = event.getChannel();
    Channel serverToDeleteFrom = server.getChannels().get(toDeleteFrom.getChannelId());
    String username = event.getUsername();
    User toDelete = server.getAllUsers().get(username);
    
    try {
      // If the user doesn't exist, send back a request failed event
      if (toDelete == null) {
        ObjectOutputStream output = server.getCurUsers().get((User) event.getSource()).getOutputStream();
        output.writeObject(new RequestFailedEvent((User) event.getSource()));
        return;
      }

      // Remove this user from the channel file.
      HashMap<String, HashMap<Integer, String>> fileInfo = new HashMap<>();
      HashMap<Integer, String> linesToFix = new HashMap<>();
      fileInfo.put("data/channels/" + toDeleteFrom.getChannelId() + ".txt", linesToFix);
      int oldNumUsers = toDeleteFrom.getUsers().size();
      linesToFix.put(5, (oldNumUsers - 1) + "\n");
      linesToFix.put(5 + oldNumUsers, "");
      server.getFileRewriteQueue().add(fileInfo);

      // Remove this channel from the user's file.
      // First, update the number of channels.
      String filePath = "data/users/" + toDelete.getUsername() + ".txt";
      File userFile = new File(filePath);
      BufferedReader fileReader = new BufferedReader(new FileReader(userFile));
      // Skip reading the lines we don't care about
      for (int i = 0; i < 3; i++) {
        fileReader.readLine();
      }
      int numChannels = Integer.parseInt(fileReader.readLine().trim());
      HashMap<String, HashMap<Integer, String>> rewriteMap = new HashMap<>();
      HashMap<Integer, String> innerMap = new HashMap<>();
      innerMap.put(4, (numChannels - 1) + "\n");
      // Then, find where the channel id is listed and remove it.
      int curId = Integer.parseInt(fileReader.readLine().trim());
      int count = 1;
      while (curId != toDeleteFrom.getChannelId()) {
        curId = Integer.parseInt(fileReader.readLine().trim());
        count++;
      }
      fileReader.close();
      innerMap.put(4 + count, "");
      rewriteMap.put(filePath, innerMap);
      server.getFileRewriteQueue().add(rewriteMap);

      serverToDeleteFrom.removeUser(toDelete);
      toDeleteFrom.removeUser(toDelete);  // technically unnecessary, but it makes sense

      // Send back a message sent event to every online user in the channel
      for (User member : serverToDeleteFrom.getUsers()) {
        // skip offline users
        if (!server.getCurUsers().containsKey(member)) continue;
        ObjectOutputStream output = server.getCurUsers().get(member).getOutputStream();
        output.writeObject(new ChannelRemoveMemberEvent((User) event.getSource(), 
                                                        serverToDeleteFrom, username));
        output.flush();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
}
