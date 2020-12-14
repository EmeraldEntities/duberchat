package duberchat.handlers.server;

import java.io.IOException;
import java.io.ObjectOutputStream;

import duberchat.chatutil.*;
import duberchat.events.FileWriteEvent;
import duberchat.events.MessageEditEvent;
import duberchat.events.SerializableEvent;
import duberchat.handlers.Handleable;
import duberchat.server.ChatServer;

/**
 * the {@code ServerMessageEditHandler} class provides the server-side
 * implementation for handling any {@code MessageEditEvent}.
 * <p>
 * <p>
 * Created <b>2020-12-08</b>
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Paula Yuan
 * @see duberchat.events.MessageEditEvent
 */
public class ServerMessageEditHandler implements Handleable {
  /** The associated server this handler is attached to. */
  private ChatServer server;

  /**
   * Constructs a new {@code ServerMessageEditHandler}.
   * 
   * @param server the server that this handler is attached to.
   */
  public ServerMessageEditHandler(ChatServer server) {
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
    MessageEditEvent event = (MessageEditEvent) newEvent;
    Message edited = event.getMessage();
    Channel channel = server.getChannels().get(edited.getChannelId());
    String source = (String) event.getSource();

    // update the server-side channel's message
    for (Message msg : channel.getMessages()) {
      if (msg.equals(edited)) {
        msg.setMessage(edited.getMessage());
        break;
      }
    }

    // update the channnel file
    int id = channel.getChannelId();
    server.getFileWriteQueue().add(new FileWriteEvent(channel, "data/channels/" + id));

    // Send back a message edit event to every online user in the channel
    for (User user : channel.getUsers().values()) {
      if (!server.getCurUsers().containsKey(user)) {
        continue;
      }
      ObjectOutputStream output = server.getCurUsers().get(user).getOutputStream();
      try {
        output.writeObject(new MessageEditEvent(source, edited));
        output.flush();
        output.reset();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    server.getServerFrame().getTextArea().append(source + " edited a message in channel " + id + ".\n");
  }
  
}
