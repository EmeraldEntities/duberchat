package duberchat.handlers.server;

import java.io.*;
import java.util.HashMap;

import duberchat.chatutil.Channel;
import duberchat.chatutil.User;
import duberchat.events.ChannelAddMemberEvent;
import duberchat.events.RequestFailedEvent;
import duberchat.events.SerializableEvent;
import duberchat.handlers.Handleable;
import duberchat.server.ChatServer;

public class ServerChannelAddMemberHandler implements Handleable {
  private ChatServer server;

  public ServerChannelAddMemberHandler(ChatServer server) {
    this.server = server;
  }
   
  public void handleEvent(SerializableEvent newEvent) {
    ChannelAddMemberEvent event = (ChannelAddMemberEvent) newEvent;
    Channel toAddTo = event.getChannel();
    Channel serverToAddTo = server.getChannels().get(toAddTo.getChannelId());
    String newUserUsername = event.getNewUserUsername();
    User toAdd = server.getAllUsers().get(newUserUsername);
    
    try {
      // If the user doesn't exist, send back a request failed event
      if (toAdd == null) {
        ObjectOutputStream output = server.getCurUsers().get((User) event.getSource()).getOutputStream();
        output.writeObject(new RequestFailedEvent((User) event.getSource()));
        return;
      }

      serverToAddTo.addUser(toAdd);

      // Lots of file updates:
      // Update this channel's file to include the new user.
      HashMap<String, HashMap<Integer, String>> fileInfo = new HashMap<>();
      HashMap<Integer, String> linesToFix = new HashMap<>();
      fileInfo.put("data/channels/" + toAddTo.getChannelId() + ".txt", linesToFix);
      int oldNumUsers = toAddTo.getUsers().size();
      linesToFix.put(5, (oldNumUsers + 1) + "\n");
      // The arraylist of users should be in the same order as the usernames are in the .txt file
      String oldLastUser = toAddTo.getUsers().get(oldNumUsers - 1).getUsername();
      linesToFix.put(5 + oldNumUsers, oldLastUser + "\n" + newUserUsername + "\n");
      server.getFileRewriteQueue().add(fileInfo);

      // Update the new member's user file to reflect their addition to the channel
      // first find and replace the current # of channels
      String filePath = "data/users/" + toAdd.getUsername() + ".txt";
      File userFile = new File(filePath);
      BufferedReader fileReader = new BufferedReader(new FileReader(userFile));
      // Skip reading the lines we don't care about
      for (int i = 0; i < 3; i++) {
        fileReader.readLine();
      }
      int numChannels = Integer.parseInt(fileReader.readLine().trim());
      fileReader.close();
      HashMap<String, HashMap<Integer, String>> rewriteMap = new HashMap<>();
      HashMap<Integer, String> innerMap = new HashMap<>();
      innerMap.put(4, (numChannels + 1) + "\n");
      rewriteMap.put(filePath, innerMap);
      server.getFileRewriteQueue().add(rewriteMap);

      // then, append the new channel id to the end of the user file
      String[] newMsg = {filePath, toAddTo.getChannelId() + "\n" };
      server.getFileAppendQueue().add(newMsg);

      // Send back a add member event to every online user in the channel
      for (User member : serverToAddTo.getUsers()) {
        if (!server.getCurUsers().containsKey(member)) continue;
        ObjectOutputStream output = server.getCurUsers().get(member).getOutputStream();
        output.writeObject(new ChannelAddMemberEvent((User) event.getSource(), serverToAddTo, newUserUsername));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

  }
}
