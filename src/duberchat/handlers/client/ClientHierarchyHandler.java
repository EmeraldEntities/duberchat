package duberchat.handlers.client;

import java.util.HashSet;

import duberchat.chatutil.Channel;
import duberchat.chatutil.User;
import duberchat.client.ChatClient;
import duberchat.events.SerializableEvent;
import duberchat.events.ChannelHierarchyChangeEvent;
import duberchat.events.FriendEvent;
import duberchat.handlers.Handleable;

/**
 * the {@code ClientHierarchyHandler} class provides the client-side
 * implementation for handling any {@code ChannelHierarchyChangeEvent}.
 * <p>
 * As hierarchy changes within a channel (promoting/demoting) have similar
 * implementations, this generic handler is designed to handle them.
 * <p>
 * Created <b>2020-12-12</b>
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Joseph Wang
 * @see duberchat.events.ChannelHierarchyChangeEvent
 * @see duberchat.events.ChannelPromoteMemberEvent
 * @see duberchat.events.ChannelRemoveMemberEvent
 */
public class ClientHierarchyHandler implements Handleable {
    /** The associated client this handler is attached to. */
    protected ChatClient client;

    /**
     * Constructs a new {@code ClientHierarchyHandler}.
     * 
     * @param client the client that this handler is attached to.
     */
    public ClientHierarchyHandler(ChatClient client) {
        this.client = client;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Ensures that the admin users of the specified channel are updated.
     * 
     * @param event {@inheritDoc}
     */
    public void handleEvent(SerializableEvent event) {
        ChannelHierarchyChangeEvent hierarchyEvent = (ChannelHierarchyChangeEvent) event;
        Channel updatedChannel = hierarchyEvent.getChannel();
        // TODO: does this kill pointers?
        HashSet<User> updatedUsers = updatedChannel.getAdminUsers();
        Channel localChannel = this.client.getChannels().get(updatedChannel.getChannelId());
        User toRemove = localChannel.getUsers().get(hierarchyEvent.getUsername());

        if (updatedUsers.contains(toRemove)) {
            updatedUsers.remove(toRemove);
        }

        this.client.getChannels().get(updatedChannel.getChannelId()).setAdminUsers(updatedUsers);
        if (this.client.hasCurrentChannel() && this.client.getCurrentChannel().equals(updatedChannel)) {
            this.client.getCurrentChannel().setAdminUsers(updatedUsers);
        }

        if (this.client.hasCurrentChannel() && this.client.getCurrentChannel().equals(updatedChannel)) {
            this.client.getMainMenuFrame().reload(event);
        }
    }
}
