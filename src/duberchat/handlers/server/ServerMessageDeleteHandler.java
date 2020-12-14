package duberchat.handlers.server;

import duberchat.events.FileWriteEvent;
import duberchat.events.MessageDeleteEvent;
import duberchat.events.SerializableEvent;
import duberchat.handlers.Handleable;
import duberchat.server.ChatServer;
import duberchat.chatutil.*;

import java.io.*;

/**
 * the {@code ServerMessageDeleteHandler} class provides the server-side
 * implementation for handling any {@code MessageDeleteEvent}.
 * <p>
 * <p>
 * Created <b>2020-12-07</b>
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Paula Yuan
 * @see duberchat.events.MessageDeleteEvent
 */
public class ServerMessageDeleteHandler implements Handleable {
  /** The associated server this handler is attached to. */
  private ChatServer server;

  /**
   * Constructs a new {@code ServerMessageDeleteHandler}.
   * 
   * @param server the server that this handler is attached to.
   */
  public ServerMessageDeleteHandler(ChatServer server) {
    this.server = server;
  }

  /**
   * {@inheritDoc}
   * <p>
   * Ensures that the server has a properly updated channel, updates files, and
   * sends the event to all relevant users.
   * 
   * @param newEvent {@inheritDoc}
   */
  public void handleEvent(SerializableEvent newEvent) {
    MessageDeleteEvent event = (MessageDeleteEvent) newEvent;
    Message toDelete = event.getMessage();
    Channel toDeleteFrom = server.getChannels().get(toDelete.getChannelId());
    int index = toDeleteFrom.getMessages().indexOf(toDelete);
    Message serverToDelete = toDeleteFrom.getMessages().remove(index);
    String source = (String) event.getSource();

    String filePath = "data/channels/" + toDeleteFrom.getChannelId();
    try {
      // delete the message from its channel file
      server.getFileWriteQueue().add(new FileWriteEvent(toDeleteFrom, filePath));

      // give a message deletion event to all online users in the channel
      for (User user : toDeleteFrom.getUsers().values()) {
        if (!server.getCurUsers().containsKey(user)) continue;
        ObjectOutputStream output = server.getCurUsers().get(user).getOutputStream();
        output.writeObject(new MessageDeleteEvent(source, serverToDelete));
        output.flush();
        output.reset();
      }
      server.getServerFrame().getTextArea().append("A message was removed from channel " + toDeleteFrom.getChannelId()
          + " by " + source + " and events sent to users\n");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
}
