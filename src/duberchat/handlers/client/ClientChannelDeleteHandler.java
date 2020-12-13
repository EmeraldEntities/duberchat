package duberchat.handlers.client;

import duberchat.client.ChatClient;
import duberchat.events.ChannelDeleteEvent;
import duberchat.events.SerializableEvent;
import duberchat.handlers.Handleable;

/**
 * the {@code ClientChannelDeleteHandler} class provides the client-side
 * implementation for handling the {@code ChannelDeleteEvent} sent back from the
 * server.
 * <p>
 * Created <b>2020-12-09</b>
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Joseph Wang
 * @see duberchat.events.ChannelDeleteEvent
 */
public class ClientChannelDeleteHandler implements Handleable {
    /** The associated client this handler is attached to. */
    protected ChatClient client;

    /**
     * Constructs a new {@code ClientChannelDeleteHandler}.
     * 
     * @param client the client that this handler is attached to.
     */
    public ClientChannelDeleteHandler(ChatClient client) {
        this.client = client;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Ensures that the client properly deletes the specified channel, and sends the
     * client back to the home screen if they are on the channel.
     * 
     * @param event {@inheritDoc}
     */
    public void handleEvent(SerializableEvent event) {
        ChannelDeleteEvent deleteEvent = (ChannelDeleteEvent) event;

        this.client.getChannels().remove(deleteEvent.getChannel().getChannelId());

        if (this.client.hasCurrentChannel() && this.client.getCurrentChannel().equals(deleteEvent.getChannel())) {
            this.client.setCurrentChannel(null);
            this.client.getMainMenuFrame().switchChannelsToCurrent();
        } else {
            this.client.getMainMenuFrame().reload(event);
        }
    }
}
