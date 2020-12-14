package duberchat.handlers.client;

import java.util.ArrayList;

import duberchat.chatutil.Channel;
import duberchat.chatutil.Message;
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
        int modifiedChannelId = memberEvent.getChannelId();
        Channel curChannel = this.client.getCurrentChannel();
        String userToRemove = memberEvent.getUsername();

        if (this.client.getUser().getUsername().equals(userToRemove)) {
            // Client user is the user to delete
            this.client.getChannels().remove(modifiedChannelId);
            this.client.getUser().getChannels().remove(modifiedChannelId);

            if (curChannel != null && curChannel.getChannelId() == modifiedChannelId) {
                this.client.setCurrentChannel(null);
                this.client.getMainMenuFrame().switchChannelsToCurrent();
            } else {
                this.client.getMainMenuFrame().reload(event);
            }
        } else {
            // Client user is not the user to delete
            Channel localChannel = this.client.getChannels().get(modifiedChannelId);
            localChannel.getUsers().remove(memberEvent.getUsername());

            // Remove all messages by this user.
            ArrayList<Message> localMessages = localChannel.getMessages();
            for (int i = localMessages.size() - 1; i >= 0; i--) {
                if (localMessages.get(i).getSenderUsername().equals(userToRemove)) {
                    localMessages.remove(i);
                }
            }

            // We only need to reload if we are currently looking at the channel
            if (curChannel != null && curChannel.getChannelId() == modifiedChannelId) {
                this.client.getMainMenuFrame().reload(event);
            }
        }

    }
}
