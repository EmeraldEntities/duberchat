package duberchat.handlers.client;

import duberchat.chatutil.Channel;
import duberchat.client.ChatClient;
import duberchat.events.SerializableEvent;
import duberchat.events.ChannelAddMemberEvent;
import duberchat.events.ChannelEvent;
import duberchat.handlers.Handleable;

/**
 * the {@code ClientChannelAddMemberHandler} class provides the client-side
 * implementation for handling any {@code ChannelAddMemberEvent}.
 * <p>
 * <p>
 * Created <b>2020-12-12</b>
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Joseph Wang
 * @see duberchat.events.ChannelAddMemberEvent
 */
public class ClientChannelAddMemberHandler implements Handleable {
    /** The associated client this handler is attached to. */
    protected ChatClient client;

    /**
     * Constructs a new {@code ClientChannelAddMemberHandler}.
     * 
     * @param client the client that this handler is attached to.
     */
    public ClientChannelAddMemberHandler(ChatClient client) {
        this.client = client;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Ensures that the client has a properly updated user list.
     * 
     * @param event {@inheritDoc}
     */
    public void handleEvent(SerializableEvent event) {
        ChannelAddMemberEvent memberEvent = (ChannelAddMemberEvent) event;
        Channel modifiedChannel = memberEvent.getChannel();

        if (!this.client.getChannels().containsKey(modifiedChannel.getChannelId())) {
            // This user is the new user
            this.client.getChannels().put(modifiedChannel.getChannelId(), modifiedChannel);
            this.client.getUser().getChannels().add(modifiedChannel.getChannelId());
            this.client.getMainMenuFrame().reload();
        } else {
            // This user is not the new user
            this.client.getChannels().get(modifiedChannel.getChannelId()).setUsers(modifiedChannel.getUsers());

            // We only need to reload if we are currently looking at the channel
            if (this.client.hasCurrentChannel() && this.client.getCurrentChannel().equals(modifiedChannel)) {
                this.client.getMainMenuFrame().reload(event);
            }
        }

    }
}
