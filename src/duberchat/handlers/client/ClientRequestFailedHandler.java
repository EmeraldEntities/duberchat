package duberchat.handlers.client;

import duberchat.client.ChatClient;

import duberchat.handlers.Handleable;

import duberchat.events.SerializableEvent;
import duberchat.events.RequestFailedEvent;
import duberchat.events.ChannelCreateEvent;
import duberchat.events.ClientRequestMessageEvent;

/**
 * the {@code ClientRequestFailedHandler} class provides the client-side
 * implementation for handling the {@code RequestFailedEvent} sent back from the
 * server.
 * <p>
 * Since {@code RequestFailedEvent} is a common generic event propagated
 * whenever any request fails, the handler will have different implementations
 * based on the type of event handled.
 * <p>
 * Created <b>2020-12-06</b>
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Joseph Wang
 * @see duberchat.events.RequestFailedEvent
 */
public class ClientRequestFailedHandler implements Handleable {
    /** The associated client this handler is attached to. */
    protected ChatClient client;

    /**
     * Constructs a new {@code ClientRequestFailedHandler}.
     * 
     * @param client the client that this handler is attached to.
     */
    public ClientRequestFailedHandler(ChatClient client) {
        this.client = client;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method will handle events different based on the source event type.
     * 
     * @param event {@inheritDoc}
     */
    public void handleEvent(SerializableEvent event) {
        RequestFailedEvent failedEvent = (RequestFailedEvent) event;

        // Something went wrong with the channel creation
        if (failedEvent.getSource() instanceof ChannelCreateEvent) {
            if (this.client.getMainMenuFrame().hasActiveChannelCreateFrame()) {
                this.client.getMainMenuFrame().getChannelCreateFrame().reload();
            }
        } else if (failedEvent.getSource() instanceof ClientRequestMessageEvent) {
            System.out.println("SYSTEM: No more messages can be requested.");
        }
    }
}
