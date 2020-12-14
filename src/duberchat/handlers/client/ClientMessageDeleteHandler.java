package duberchat.handlers.client;

import duberchat.chatutil.Channel;
import duberchat.chatutil.Message;
import duberchat.client.ChatClient;
import duberchat.events.MessageDeleteEvent;
import duberchat.events.SerializableEvent;
import duberchat.handlers.Handleable;

/**
 * the {@code ClientMessageDeleteHandler} class provides the client-side
 * implementation for handling the {@code MessageDeleteEvent} sent back from the
 * server.
 * <p>
 * Created <b>2020-12-11</b>
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Joseph Wang
 * @see duberchat.events.MessageDeleteEvent
 */
public class ClientMessageDeleteHandler implements Handleable {
    /** The associated client this handler is attached to. */
    protected ChatClient client;

    /**
     * Constructs a new {@code ClientMessageDeleteHandler}.
     * 
     * @param client the client that this handler is attached to.
     */
    public ClientMessageDeleteHandler(ChatClient client) {
        this.client = client;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Ensures that the client properly removes the message that was deleted, and
     * refreshes the text display to make sure all instances of said message are
     * deleted.
     * 
     * @param event {@inheritDoc}
     */
    public void handleEvent(SerializableEvent event) {
        MessageDeleteEvent msgEvent = (MessageDeleteEvent) event;
        Message message = msgEvent.getMessage();
        Channel localChannel = client.getChannels().get(message.getChannelId());

        // Remove the message regardless of sender to ensure a proper synchronized
        // message list
        // We can assume that since we got this event, we have this channel
        localChannel.getMessages().remove(message);

        if (client.hasCurrentChannel() && client.getCurrentChannel().equals(localChannel)) {
            client.getMainMenuFrame().reload(event);
        }
    }
}
