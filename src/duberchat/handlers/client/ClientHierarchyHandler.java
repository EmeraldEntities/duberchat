package duberchat.handlers.client;

import duberchat.chatutil.Channel;
import duberchat.chatutil.User;
import duberchat.client.ChatClient;
import duberchat.events.SerializableEvent;
import duberchat.events.ChannelHierarchyChangeEvent;
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
        int updatedChannelId = hierarchyEvent.getChannelId();

        Channel localChannel = this.client.getChannels().get(updatedChannelId);
        User toHandle = localChannel.getUsers().get(hierarchyEvent.getUsername());
        Channel curChannel = this.client.getCurrentChannel();

        if (localChannel.getAdminUsers().contains(toHandle)) {
            // Demoting this user
            localChannel.getAdminUsers().remove(toHandle);
            if (curChannel != null && curChannel.getChannelId() == updatedChannelId) {
                // Just in case these are not linked for some reason
                // If they are then this has no effect
                this.client.getCurrentChannel().getAdminUsers().remove(toHandle);
                this.client.getMainMenuFrame().reload(event);
            }
        } else {
            // Promoting this user
            localChannel.getAdminUsers().add(toHandle);

            if (curChannel != null && curChannel.getChannelId() == updatedChannelId) {
                this.client.getCurrentChannel().getAdminUsers().add(toHandle);
                this.client.getMainMenuFrame().reload(event);
            }
        }

        if (curChannel != null && curChannel.getChannelId() == updatedChannelId) {
            // Reload the channel to reflect actual changes
            this.client.getMainMenuFrame().reload(event);
        }
    }
}
