package duberchat.handlers.client;

import duberchat.chatutil.User;
import duberchat.client.ChatClient;
import duberchat.events.SerializableEvent;
import duberchat.events.FriendEvent;
import duberchat.events.FriendAddEvent;
import duberchat.handlers.Handleable;

/**
 * the {@code ClientFriendAdjustHandler} class provides the client-side
 * implementation for handling any {@code FriendEvent}.
 * <p>
 * Since adding/removing friends have very similar implementations, one generic
 * handler is able to resolve both events.
 * <p>
 * Created <b>2020-12-12</b>
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Joseph Wang
 * @see duberchat.events.FriendEvent
 * @see duberchat.events.FriendAddEvent
 * @see duberchat.events.FriendRemoveEvent
 */
public class ClientFriendAdjustHandler implements Handleable {
    /** The associated client this handler is attached to. */
    protected ChatClient client;

    /**
     * Constructs a new {@code ClientFriendAdjustHandler}.
     * 
     * @param client the client that this handler is attached to.
     */
    public ClientFriendAdjustHandler(ChatClient client) {
        this.client = client;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Ensures that the client has a properly updated friends list, and ensures that
     * list is promptly updated if active.
     * 
     * @param event {@inheritDoc}
     */
    public void handleEvent(SerializableEvent event) {
        FriendEvent friendEvent = (FriendEvent) event;
        String friendUsername = friendEvent.getFriendUsername();

        if (this.client.getFriends().containsKey(friendUsername)) {
            this.client.getFriends().remove(friendUsername);
        } else {
            User friend = ((FriendAddEvent) friendEvent).getFriend();
            this.client.getFriends().put(friend.getUsername(), friend);
        }

        if (!this.client.hasCurrentChannel() && this.client.hasMainMenuFrame()) {
            this.client.getMainMenuFrame().reload(event);
        }
    }
}
