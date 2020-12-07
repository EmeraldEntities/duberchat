package duberchat.handlers.server;

import java.io.IOException;
import java.io.ObjectOutputStream;
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
      }
      // Add the new message with the appropriate file path to the file write queue.
      // Messages are formatted like this: id tismeStamp senderUsername msg
      HashMap<String, HashMap<Integer, String>> fileInfo = new HashMap<>();
      HashMap<Integer, String> linesToFix = new HashMap<>();
      fileInfo.put("data/channels/" + toAddTo.getChannelId() + ".txt", linesToFix);
      int oldNumUsers = toAddTo.getUsers().size();
      linesToFix.put(5, (oldNumUsers + 1) + "\n");
      // The arraylist of users should be in the same order as the usernames are in the .txt file
      String oldLastUser = toAddTo.getUsers().get(oldNumUsers - 1).getUsername();
      linesToFix.put(5 + oldNumUsers, oldLastUser + "\n" + newUserUsername + "\n");
      server.getFileRewriteQueue().add(fileInfo);

      serverToAddTo.addUser(toAdd);

      // Send back a message sent event to every online user in the channel
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
