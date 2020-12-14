package duberchat.handlers.server;

import java.io.*;
import java.util.Iterator;
import java.util.*;

import duberchat.chatutil.*;
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
    int id = event.getChannelId();
    Channel toAddTo = server.getChannels().get(id);
    String newUserUsername = event.getNewUserUsername();
    User toAdd = server.getAllUsers().get(newUserUsername);
    User source = server.getAllUsers().get((String) event.getSource());
    
    try {
      // If the user doesn't exist, send back a request failed event
      if (toAdd == null) {
        ObjectOutputStream output = server.getCurUsers().get(source).getOutputStream();
        output.writeObject(new RequestFailedEvent(source.getUsername()));
        output.flush();
        output.reset();
        server.getServerFrame().getTextArea()
            .append(source.getUsername() + " tried to add an nonexistent user to a channel\n");
        return;
      }
      
      toAdd.getChannels().add(id);
      toAddTo.addUser(toAdd);

      // Update this channel's file to include the new user.
      String channelFilePath = "data/channels/" + id;
      server.getFileWriteQueue().add(new FileWriteEvent(toAddTo, channelFilePath));

      // Update the new member's user file to reflect their addition to the channel
      String userFilePath = "data/users/" + toAdd.getUsername();
      server.getFileWriteQueue().add(new FileWriteEvent(toAdd, userFilePath));

      // Send back a add member event to every online user in the channel
      for (User u : toAddTo.getUsers().values()) {
          if (!server.getCurUsers().containsKey(u)) {
              continue;
          }

          ObjectOutputStream output = server.getCurUsers().get(u).getOutputStream();
          event.setNewChannel(toAddTo);
          event.setNewUser(toAdd);

          // output.writeObject(new ChannelAddMemberEvent(source.getUsername(), id,
          // newUserUsername,
          // toAdd, newChannel));
          output.writeObject(event);
          output.flush();
          output.reset();
      }

      // Iterator<User> itr = toAddTo.getUsers().values().iterator();
      // while (itr.hasNext()) {
      // User member = itr.next();
      // // skip offline users
      // if (!server.getCurUsers().containsKey(member)) {
      // continue;
      // }
      // ObjectOutputStream output =
      // server.getCurUsers().get(member).getOutputStream();

      // output.writeObject(new ChannelAddMemberEvent(source.getUsername(), id,
      // newUserUsername,
      // toAdd, toAddTo));
      // output.flush();
      // output.reset();
      // }
      server.getServerFrame().getTextArea().append(
          newUserUsername + " added to channel " + id + " by " + source.getUsername() + " and events sent to users\n");
    } catch (IOException e) {
      e.printStackTrace();
    }

  }
}
