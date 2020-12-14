package duberchat.handlers.client;

import duberchat.client.ChatClient;
import duberchat.events.ClientRequestMessageEvent;
import duberchat.events.SerializableEvent;
import duberchat.handlers.Handleable;

/**
 * the {@code ClientRequestMessageHandler} class provides the client-side
 * implementation for handling the {@code ClientMessageRequestEvent} sent back
 * from the server.
 * <p>
 * Created <b>2020-12-11</b>
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Joseph Wang
 * @see duberchat.events.ClientRequestMessageEvent
 */
public class ClientRequestMessageHandler implements Handleable {
    /** The associated client this handler is attached to. */
    protected ChatClient client;

    /**
     * Constructs a new {@code ClientRequestMessageHandler}.
     * 
     * @param client the client that this handler is attached to.
     */
    public ClientRequestMessageHandler(ChatClient client) {
        this.client = client;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Ensures that the appended messages can be shown and that the client can see
     * them.
     * 
     * @param event {@inheritDoc}
     */
    public void handleEvent(SerializableEvent event) {
        ClientRequestMessageEvent msgEvent = (ClientRequestMessageEvent) event;
        int updatedChannelId = msgEvent.getChannelId();

        // Load the message block
        this.client.getChannels().get(updatedChannelId).loadMessageCluster(msgEvent.getNewMessageBlock());

        if (this.client.hasCurrentChannel() && this.client.getCurrentChannel().getChannelId() == updatedChannelId) {
            // Properly resetting pointer in case something went wrong in between
            this.client.setCurrentChannel(this.client.getChannels().get(updatedChannelId));

            if (this.client.hasMainMenuFrame()) {
                this.client.getMainMenuFrame().reload(event);
            }
        }
    }
}
