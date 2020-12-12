package duberchat.handlers.client;

import duberchat.chatutil.Channel;
import duberchat.client.ChatClient;
import duberchat.events.SerializableEvent;
import duberchat.events.ChannelRemoveMemberEvent;
import duberchat.handlers.Handleable;

/**
 * the {@code ClientChannelRemoveMemberHandler} class provides the client-side
 * implementation for handling any {@code ChannelRemoveMemberEvent}.
 * <p>
 * <p>
 * Created <b>2020-12-12</b>
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Joseph Wang
 * @see duberchat.events.ChannelRemoveMemberEvent
 */
public class ClientChannelRemoveMemberHandler implements Handleable {
    /** The associated client this handler is attached to. */
    protected ChatClient client;

    /**
     * Constructs a new {@code ClientChannelRemoveMemberHandler}.
     * 
     * @param client the client that this handler is attached to.
     */
    public ClientChannelRemoveMemberHandler(ChatClient client) {
        this.client = client;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Ensures that the client has a properly updated user list, * if necessary.
     * 
     * 
     * @param event {@inheritDoc}
     */
    public void handleEvent(SerializableEvent event) {
        ChannelRemoveMemberEvent memberEvent = (ChannelRemoveMemberEvent) event;
        Channel modifiedChannel = memberEvent.getChannel();

        if (this.client.getUser().getUsername().equals(memberEvent.getUsername())) {
            // This user is the user to delete
            this.client.getChannels().remove(modifiedChannel.getChannelId());
            this.client.getUser().getChannels().remove(modifiedChannel.getChannelId());
            this.client.getMainMenuFrame().reload();
        } else {
            // This user is not the user to delete
            this.client.getChannels().get(modifiedChannel.getChannelId()).setUsers(modifiedChannel.getUsers());

            // We only need to reload if we are currently looking at the channel
            if (this.client.hasCurrentChannel() && this.client.getCurrentChannel().equals(modifiedChannel)) {
                this.client.getMainMenuFrame().reload(event);
            }
        }

    }
}
