package duberchat.handlers.client;

import duberchat.chatutil.Channel;
import duberchat.chatutil.Message;
import duberchat.client.ChatClient;
import duberchat.events.MessageSentEvent;
import duberchat.events.SerializableEvent;
import duberchat.handlers.Handleable;

/**
 * the {@code ClientMessageSentHandler} class provides the client-side
 * implementation for handling the {@code MessageSentEvent} sent back from the
 * server.
 * <p>
 * Created <b>2020-12-05</b>
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Joseph Wang
 * @see duberchat.events.MessageSentEvent
 */
public class ClientMessageSentHandler implements Handleable {
    /** The associated client this handler is attached to. */
    protected ChatClient client;

    /**
     * Constructs a new {@code ClientMessageSentHandler}.
     * 
     * @param client the client that this handler is attached to.
     */
    public ClientMessageSentHandler(ChatClient client) {
        this.client = client;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Ensures that the client creates and displays the new message sent by a user.
     * This method will be one of the most commonly called ones.
     * 
     * @param event {@inheritDoc}
     */
    public void handleEvent(SerializableEvent event) {
        MessageSentEvent msgEvent = (MessageSentEvent) event;
        Message message = msgEvent.getMessage();

        Channel localChannel = client.getChannels().get(message.getChannel().getChannelId());

        localChannel.addMessage(message);

        if (client.hasCurrentChannel() && client.getCurrentChannel().equals(localChannel)) {
            client.getMainMenuFrame().reload(event);
        }
    }
}
