package duberchat.handlers.server;

import duberchat.events.ChannelRemoveMemberEvent;
import duberchat.events.FileWriteEvent;
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
    int id = toDeleteFrom.getChannelId();
    
    try {
      // If the user doesn't exist, send back a request failed event
      if (toDelete == null) {
        ObjectOutputStream output = server.getCurUsers().get((User) event.getSource()).getOutputStream();
        output.writeObject(new RequestFailedEvent((User) event.getSource()));
        return;
      }

      serverToDeleteFrom.removeUser(toDelete);
      toDeleteFrom.removeUser(toDelete);  // technically unnecessary? TODO
      toDelete.getChannels().remove(id);

      // Remove this user from the channel file.
      String channelFilePath = "data/channels/" + id + ".txt";
      server.getFileWriteQueue().add(new FileWriteEvent(serverToDeleteFrom, channelFilePath));
      // Remove this channel from the user's file.
      String userFilePath = "data/users/" + toDelete.getUsername() + ".txt";
      server.getFileWriteQueue().add(new FileWriteEvent(toDelete, userFilePath));

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
