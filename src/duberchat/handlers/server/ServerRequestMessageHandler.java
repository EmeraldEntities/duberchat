package duberchat.handlers.server;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import duberchat.chatutil.Channel;
import duberchat.chatutil.Message;
import duberchat.chatutil.User;
import duberchat.events.ClientRequestMessageEvent;
import duberchat.events.RequestFailedEvent;
import duberchat.events.SerializableEvent;
import duberchat.handlers.Handleable;
import duberchat.server.ChatServer;

/**
 * the {@code ServerRequestMessageHandler} class provides the server-side
 * implementation for handling any {@code ClientRequestMessageEvent}.
 * <p>
 * <p>
 * Created <b>2020-12-08</b>
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Paula Yuan
 * @see duberchat.events.ClientRequestMessageEvent
 */
public class ServerRequestMessageHandler implements Handleable {
  /** The associated server this handler is attached to. */
  private ChatServer server;

  /**
   * Constructs a new {@code ServerRequestMessageHandler}.
   * 
   * @param server the server that this handler is attached to.
   */
  public ServerRequestMessageHandler(ChatServer server) {
    this.server = server;
  }

  /**
   * {@inheritDoc}
   * <p>
   * Ensures that the server sends the event with the requested messages to the
   * requestor.
   * 
   * @param newEvent {@inheritDoc}
   */
  public void handleEvent(SerializableEvent newEvent) {
    ClientRequestMessageEvent event = (ClientRequestMessageEvent) newEvent;
    String source = (String) event.getSource();
    User user = server.getAllUsers().get(source);
    ObjectOutputStream output = server.getCurUsers().get(user).getOutputStream();

    int lastMessageId = event.getStartMsgId();
    Channel channel = server.getChannels().get(event.getChannelId());
    ArrayList<Message> messages = channel.getMessages();
    ArrayList<Message> messageBlock = new ArrayList<>();

    // loop through messages to find the starting section.
    for (int i = messages.size() - 1; i >= 0; i--) {
      Message curMessage = messages.get(i);

      // remember, new messages go at the end, so old messages go to the top
      if (curMessage.getMessageId() == lastMessageId) {
        int startMsgId = messages.get(Math.max(i - 1, 0)).getMessageId();
        // Add the next thirty messages to the message block
        for (int j = i - 1; j >= Math.max(i - 30, 0); j--) {
          messageBlock.add(0, messages.get(j));
        }

        // Send back the event to the user who requested messages.
        try {
          output.writeObject(new ClientRequestMessageEvent(source, startMsgId, channel.getChannelId(), messageBlock));
          output.flush();
          output.reset();
        } catch (IOException e) {
          e.printStackTrace();
        }
        server.getServerFrame().getTextArea()
            .append(source + " requested messages. Messages found, event sent to user.\n");
        return;
      }
    }

    // Send back a request failed if the message can't be found or the bounds are illegal.
    try {
      output.writeObject(new RequestFailedEvent(source));
      output.flush();
      output.reset();
      server.getServerFrame().getTextArea()
          .append(source + " requested messages. Request failed, sent event to users.\n");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
}
