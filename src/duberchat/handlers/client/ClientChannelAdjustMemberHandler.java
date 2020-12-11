package duberchat.handlers.client;

import duberchat.chatutil.Channel;
import duberchat.client.ChatClient;
import duberchat.events.SerializableEvent;
import duberchat.events.ChannelEvent;
import duberchat.handlers.Handleable;

/**
 * the {@code ClientChannelAdjustMemberHandler} class provides the client-side
 * implementation for handling any {@code ChannelEvent} that involves changing
 * the user list of the associated channel.
 * <p>
 * As users are processed, added, removed, and validated by the server which
 * then propagates the event with the adjusted channel with the adjusted users,
 * the client does not need to do any of that. As a result, the implementation
 * of every channel user event on the client side is practically identical. This
 * handler serves as a general handler that serves multiple types of said
 * similar events.
 * <p>
 * Created <b>2020-12-06</b>
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Joseph Wang
 * @see duberchat.events.ChannelEvent
 * @see duberchat.events.ChannelAddMemberEvent
 * @see duberchat.events.ChannelRemoveMemberEvent
 */
public class ClientChannelAdjustMemberHandler implements Handleable {
    /** The associated client this handler is attached to. */
    protected ChatClient client;

    /**
     * Constructs a new {@code ClientChannelAdjustMemberHandler}.
     * 
     * @param client the client that this handler is attached to.
     */
    public ClientChannelAdjustMemberHandler(ChatClient client) {
        this.client = client;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Ensures that the client has the updated channel user list, regardless of the
     * member adjust function.
     * 
     * @param event {@inheritDoc}
     */
    public void handleEvent(SerializableEvent event) {
        ChannelEvent memberEvent = (ChannelEvent) event;
        Channel modifiedChannel = memberEvent.getChannel();

        // Make sure we never have to deal with NPE if server sends smth bad
        if (!this.client.getChannels().containsKey(modifiedChannel.getChannelId())) {
            return;
        }

        this.client.getChannels().get(modifiedChannel.getChannelId()).setUsers(modifiedChannel.getUsers());

        // We only need to reload if we are currently looking at the channel
        if (this.client.hasCurrentChannel() && this.client.getCurrentChannel().equals(modifiedChannel)) {
            this.client.getMainMenuFrame().reload(event);
        }
    }
}
