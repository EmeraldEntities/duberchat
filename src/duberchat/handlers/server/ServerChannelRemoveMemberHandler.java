package duberchat.handlers.server;

import duberchat.events.ChannelRemoveMemberEvent;
import duberchat.events.FileWriteEvent;
import duberchat.events.RequestFailedEvent;
import duberchat.events.SerializableEvent;
import duberchat.handlers.Handleable;
import duberchat.server.ChatServer;

import java.io.*;
import java.util.Iterator;

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
    int id = toDeleteFrom.getChannelId();
    Channel serverToDeleteFrom = server.getChannels().get(id);
    String username = event.getUsername();
    User toDelete = server.getAllUsers().get(username);
    
    try {
      // If the user doesn't exist, send back a request failed event
      if (toDelete == null) {
        ObjectOutputStream output = server.getCurUsers().get((User) event.getSource()).getOutputStream();
        output.writeObject(new RequestFailedEvent(new User(toDelete)));
        output.flush();
        return;
      }

      serverToDeleteFrom.removeUser(toDelete);
      toDeleteFrom.removeUser(toDelete);  // technically unnecessary? TODO
      toDelete.getChannels().remove(id);

      // If this channel has no more users, purge it from the server and delete its file.
      // Otherwise, remove the user from the channel file.
      String channelFilePath = "data/channels/" + id;
      if (serverToDeleteFrom.getUsers().size() <= 0) {
        server.getChannels().remove(id);
        File channelFile = new File(channelFilePath);
        channelFile.delete();
      } else {
        server.getFileWriteQueue().add(new FileWriteEvent(serverToDeleteFrom, channelFilePath));
      }

      // Remove this channel from the user's file.
      String userFilePath = "data/users/" + toDelete.getUsername();
      server.getFileWriteQueue().add(new FileWriteEvent(toDelete, userFilePath));

      // Send back a message sent event to every online user in the channel
      Iterator<User> itr = serverToDeleteFrom.getUsers().values().iterator();
      while (itr.hasNext()) {
        User member = itr.next();
        // skip offline users
        if (!server.getCurUsers().containsKey(member)) continue;
        ObjectOutputStream output = server.getCurUsers().get(member).getOutputStream();
        output.writeObject(new ChannelRemoveMemberEvent(new User(toDelete), 
                                                        new Channel(serverToDeleteFrom), 
                                                        username));
        output.flush();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
}
