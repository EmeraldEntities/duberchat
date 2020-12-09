package duberchat.handlers.server;

import java.io.*;
import java.util.HashMap;

import duberchat.chatutil.Channel;
import duberchat.chatutil.User;
import duberchat.events.ChannelAddMemberEvent;
import duberchat.events.FileWriteEvent;
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
    int id = toAddTo.getChannelId();
    
    try {
      // If the user doesn't exist, send back a request failed event
      if (toAdd == null) {
        ObjectOutputStream output = server.getCurUsers().get((User) event.getSource()).getOutputStream();
        output.writeObject(new RequestFailedEvent((User) event.getSource()));
        return;
      }
      
      serverToAddTo.addUser(toAdd);
      toAddTo.addUser(toAdd);   // technically unnecessary? TODO
      toAdd.getChannels().add(id);

      // Update this channel's file to include the new user.
      String channelFilePath = "data/channels/" + id + ".txt";
      server.getFileWriteQueue().add(new FileWriteEvent(serverToAddTo, channelFilePath));

      // Update the new member's user file to reflect their addition to the channel
      String userFilePath = "data/users/" + toAdd.getUsername() + ".txt";
      server.getFileWriteQueue().add(new FileWriteEvent(toAdd, userFilePath));

      // Send back a add member event to every online user in the channel
      for (User member : serverToAddTo.getUsers()) {
        // skip offline users
        if (!server.getCurUsers().containsKey(member)) continue;
        ObjectOutputStream output = server.getCurUsers().get(member).getOutputStream();
        output.writeObject(new ChannelAddMemberEvent((User) event.getSource(), serverToAddTo, 
                                                     newUserUsername));
        output.flush();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

  }
}
