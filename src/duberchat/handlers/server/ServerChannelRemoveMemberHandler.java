package duberchat.handlers.server;

import duberchat.events.ChannelRemoveMemberEvent;
import duberchat.events.FileWriteEvent;
import duberchat.events.RequestFailedEvent;
import duberchat.events.SerializableEvent;
import duberchat.handlers.Handleable;
import duberchat.server.ChatServer;

import java.io.*;
import java.util.ArrayList;

import duberchat.chatutil.Channel;
import duberchat.chatutil.Message;
import duberchat.chatutil.User;

/**
 * the {@code ServerChannelRemoveMemberHandler} class provides the server-side
 * implementation for handling any {@code ChannelRemoveMemberEvent}.
 * <p>
 * <p>
 * Created <b>2020-12-06</b>
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Paula Yuan 
 * @see duberchat.events.ChannelRemoveMemberEvent
 */
public class ServerChannelRemoveMemberHandler implements Handleable {
  /** The associated server this handler is attached to. */
  private ChatServer server;

  /**
   * Constructs a new {@code ServerChannelRemoveMemberHandler}.
   * 
   * @param server the server that this handler is attached to.
   */
  public ServerChannelRemoveMemberHandler(ChatServer server) {
    this.server = server;
  }

  /**
   * {@inheritDoc}
   * <p>
   * Ensures that the server channel/user is properly updated, updates files, and
   * sends the event to all relevant users.
   * 
   * @param newEvent {@inheritDoc}
   */
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
      for (User member : toDeleteFrom.getUsers().values()) {
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
