package duberchat.handlers.server;

import java.io.*;
import java.util.HashMap;

import duberchat.events.ChannelDeleteEvent;
import duberchat.events.SerializableEvent;
import duberchat.handlers.Handleable;
import duberchat.server.ChatServer;
import duberchat.chatutil.Channel;
import duberchat.chatutil.User;

public class ServerChannelDeleteHandler implements Handleable {
  private ChatServer server;

  public ServerChannelDeleteHandler(ChatServer server) {
    this.server = server;
  }

  public void handleEvent(SerializableEvent newEvent) {
    ChannelDeleteEvent event = (ChannelDeleteEvent) newEvent;
    int toDeleteId = event.getChannel().getChannelId();
    Channel serverToDelete = server.getChannels().get(toDeleteId);

    try {
      // Remove the channel from all its users' files
      for (User user : serverToDelete.getUsers()) {
        // First, update the number of channels.
        String filePath = "data/users/" + user.getUsername() + ".txt";
        File userFile = new File(filePath);
        BufferedReader fileReader = new BufferedReader(new FileReader(userFile));
        // Skip the lines we don't care about
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
        while (curId != toDeleteId) {
          curId = Integer.parseInt(fileReader.readLine().trim());
          count++;
        }
        fileReader.close();
        innerMap.put(4 + count, "");
        rewriteMap.put(filePath, innerMap);
        server.getFileRewriteQueue().add(rewriteMap);

        // Give back a channel deletion event to all currently online users in the channel
        if (!server.getCurUsers().containsKey(user)) continue;
        ObjectOutputStream output = server.getCurUsers().get(user).getOutputStream();
        output.writeObject(new ChannelDeleteEvent((User) event.getSource(), serverToDelete));
        output.flush();
      }

      //Remove the channel file
      File channelFile = new File("data/channels/" + toDeleteId + ".txt");
      channelFile.delete();
    } catch (IOException e) {
      e.printStackTrace();
    }
    
    // remove the channel from the server's map of channels
    server.getChannels().remove(toDeleteId);
  }
  
}
