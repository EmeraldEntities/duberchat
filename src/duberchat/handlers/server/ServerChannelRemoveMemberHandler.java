package duberchat.handlers.server;

import duberchat.events.ChannelRemoveMemberEvent;
import duberchat.events.FileWriteEvent;
import duberchat.events.RequestFailedEvent;
import duberchat.events.SerializableEvent;
import duberchat.handlers.Handleable;
import duberchat.server.ChatServer;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

import duberchat.chatutil.Channel;
import duberchat.chatutil.Message;
import duberchat.chatutil.User;

public class ServerChannelRemoveMemberHandler implements Handleable {
  private ChatServer server;

  public ServerChannelRemoveMemberHandler(ChatServer server) {
    this.server = server;
  }

  public void handleEvent(SerializableEvent newEvent) {
    ChannelRemoveMemberEvent event = (ChannelRemoveMemberEvent) newEvent;
    int id = event.getChannelId();
    Channel toDeleteFrom = server.getChannels().get(id);
    String username = event.getUsername();
    User toDelete = server.getAllUsers().get(username);
    User source = server.getAllUsers().get((String) event.getSource());

    try {
      // If the user doesn't exist, send back a request failed event
      if (toDelete == null) {
        ObjectOutputStream output = server.getCurUsers().get(source).getOutputStream();
        output.writeObject(new RequestFailedEvent(source.getUsername()));
        output.flush();
        output.reset();
        server.getServerFrame().getTextArea()
            .append(source.getUsername() + " tried to remove a nonexistent user. Sent request failed event\n");
        return;
      }

      toDeleteFrom.removeUser(toDelete);
      toDelete.getChannels().remove(id);

      // Send back a message sent event to every online user in the channel, as well
      // as the removed user.
      ObjectOutputStream output = server.getCurUsers().get(toDelete).getOutputStream();
      output.writeObject(new ChannelRemoveMemberEvent(source.getUsername(), id, username));
      output.flush();
      output.reset();
      Iterator<User> itr = toDeleteFrom.getUsers().values().iterator();
      while (itr.hasNext()) {
        User member = itr.next();
        // skip offline users
        if (!server.getCurUsers().containsKey(member)) continue;
        output = server.getCurUsers().get(member).getOutputStream();
        output.writeObject(new ChannelRemoveMemberEvent(source.getUsername(), id, username)); 
        output.flush();
        output.reset();
      }
      server.getServerFrame().getTextArea().append(
          username + " removed from channel " + id + " by " + source.getUsername() + " and events sent to users\n");

      // Remove all the deleted user's messages
      ArrayList<Message> allMessages = toDeleteFrom.getMessages();
      for (int i = allMessages.size() - 1; i >= 0; i--) {
          if (allMessages.get(i).getSenderUsername().equals(toDelete.getUsername())) {
              allMessages.remove(i);
          }
      }

      // If this channel has no more users, purge it from the server and delete its
      // file. Otherwise, remove the user from the channel file.
      String channelFilePath = "data/channels/" + id;
      if (toDeleteFrom.getUsers().size() <= 0) {
        server.getChannels().remove(id);
        File channelFile = new File(channelFilePath);
        channelFile.delete();
      } else {
        server.getFileWriteQueue().add(new FileWriteEvent(toDeleteFrom, channelFilePath));
      }

      // Remove this channel from the user's file.
      String userFilePath = "data/users/" + toDelete.getUsername();
      server.getFileWriteQueue().add(new FileWriteEvent(toDelete, userFilePath));

    } catch (IOException e) {
      e.printStackTrace();
    }

  }
  
}
