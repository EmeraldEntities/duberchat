package duberchat.handlers.client;

import duberchat.chatutil.Channel;
import duberchat.chatutil.User;
import duberchat.handlers.Handleable;
import duberchat.client.ChatClient;
import duberchat.events.ChannelCreateEvent;
import duberchat.events.SerializableEvent;

/**
 * the {@code ClientChannelCreateHandler} class provides the client-side
 * implementation for handling the {@code ChannelCreateEvent} sent back from the
 * server.
 * <p>
 * Created <b>2020-12-06</b>
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Joseph Wang
 * @see duberchat.events.ChannelCreateEvent
 */
public class ClientChannelCreateHandler implements Handleable {
    /** The associated client this handler is attached to. */
    protected ChatClient client;

    /**
     * Constructs a new {@code ClientChannelCreateHandler}.
     * 
     * @param client the client that this handler is attached to.
     */
    public ClientChannelCreateHandler(ChatClient client) {
        this.client = client;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Ensures that the client creates and properly displays the new channel
     * provided on the side panel, or as its current channel if this client was the
     * creator.
     * 
     * @param event {@inheritDoc}
     */
    public void handleEvent(SerializableEvent event) {
        ChannelCreateEvent channelEvent = (ChannelCreateEvent) event;
        boolean isCreator = this.client.getUser().equals(((User) channelEvent.getSource()));

        // Make sure the channel create frame is closed if this request succeeded
        if (isCreator && this.client.getMainMenuFrame().hasActiveChannelCreateFrame()) {
            this.client.getMainMenuFrame().closeChannelCreateFrame();
        }

        Channel newChannel = channelEvent.getChannel();
        int newChannelId = newChannel.getChannelId();

        this.client.getUser().getChannels().add(newChannelId);
        // Make sure if we already have this channel that we don't add a secondary
        // channel
        if (!this.client.getChannels().containsKey(newChannel.getChannelId())) {
            this.client.getChannels().put(newChannelId, newChannel);
        } 

        // Make sure other users don't get forcefully pulled into the new
        // channel if they weren't the creator
        if (isCreator) {
            // No need to access hashmap as this channel should be a reference to the same
            // object
            this.client.setCurrentChannel(newChannel);
        }
        
        // Force a refresh of the main menu frame
        this.client.getMainMenuFrame().reload(event);
    }
}
