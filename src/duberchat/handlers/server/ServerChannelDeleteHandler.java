package duberchat.handlers.server;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;

import duberchat.events.ChannelDeleteEvent;
import duberchat.events.FileWriteEvent;
import duberchat.events.SerializableEvent;
import duberchat.handlers.Handleable;
import duberchat.server.ChatServer;
import duberchat.chatutil.Channel;
import duberchat.chatutil.User;

/**
 * the {@code ServerChannelDeleteHandler} class provides the server-side
 * implementation for handling any {@code ChannelDeleteEvent}.
 * <p>
 * <p>
 * Created <b>2020-12-06</b>
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Paula Yuan 
 * @see duberchat.events.ChannelDeleteEvent
 */
public class ServerChannelDeleteHandler implements Handleable {
  /** The associated server this handler is attached to. */
  private ChatServer server;

  /**
   * Constructs a new {@code ServerChannelDeleteHandler}.
   * 
   * @param server the server that this handler is attached to.
   */
  public ServerChannelDeleteHandler(ChatServer server) {
    this.server = server;
  }

  /**
   * {@inheritDoc}
   * <p>
   * Ensures that the server has a properly updated channel list, updates files,
   * and sends events to all relevant users.
   * 
   * @param event {@inheritDoc}
   */
  public void handleEvent(SerializableEvent newEvent) {
    ChannelDeleteEvent event = (ChannelDeleteEvent) newEvent;
    int toDeleteId = event.getChannelId();
    Channel toDelete = server.getChannels().get(toDeleteId);

    try {
      // Remove the channel from all its users and their files
      for (User user : toDelete.getUsers().values()) {
        user.getChannels().remove(toDeleteId);
        String filePath = "data/users/" + user.getUsername();
        server.getFileWriteQueue().add(new FileWriteEvent(user, filePath));

        // Give back a channel deletion event to all currently online users in the channel
        if (!server.getCurUsers().containsKey(user)) continue;
        ObjectOutputStream output = server.getCurUsers().get(user).getOutputStream();
        output.writeObject(new ChannelDeleteEvent(event.getSource(), toDeleteId));
        output.flush();
        output.reset();
      }
      server.getServerFrame().getTextArea()
          .append("Sent channel deletion events to all users in channel" + toDeleteId + "\n");

      //Remove the channel file
      File channelFile = new File("data/channels/" + toDeleteId);
      channelFile.delete();
      server.getServerFrame().getTextArea().append("channel " + toDeleteId + "file was deleted\n");
    } catch (IOException e) {
      e.printStackTrace();
    }
    
    // remove the channel from the server's map of channels
    server.getChannels().remove(toDeleteId);
  }
  
}
