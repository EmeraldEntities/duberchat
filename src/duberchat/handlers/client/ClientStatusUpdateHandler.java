package duberchat.handlers.client;

import duberchat.chatutil.Channel;
import duberchat.chatutil.User;
import duberchat.client.ChatClient;
import duberchat.events.ClientStatusUpdateEvent;
import duberchat.events.SerializableEvent;
import duberchat.handlers.Handleable;

/**
 * the {@code ClientStatusUpdateHandler} class provides the client-side
 * implementation for handling the {@code ClientStatusUpdateEvent} sent back
 * from the server.
 * <p>
 * Created <b>2020-12-09</b>
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Joseph Wang
 * @see duberchat.events.ClientStatusUpdateEvent
 */
public class ClientStatusUpdateHandler implements Handleable {
    /** The associated client this handler is attached to. */
    protected ChatClient client;

    /**
     * Constructs a new {@code ClientStatusUpdateHandler}.
     * 
     * @param client the client that this handler is attached to.
     */
    public ClientStatusUpdateHandler(ChatClient client) {
        this.client = client;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Ensures that every instance of the specified user is changed to have that
     * status. Since every instance must be changed, this method will be slightly
     * slower compared to other handlers as it must find all instances of the user
     * first.
     * 
     * @param event {@inheritDoc}
     */
    public void handleEvent(SerializableEvent event) {
        ClientStatusUpdateEvent statusEvent = (ClientStatusUpdateEvent) event;
        User user = (User) statusEvent.getSource();

        // If i initialized it, the source would be from me
        if (this.client.getUser().equals(user)) {
            this.client.getUser().setStatus(statusEvent.getStatus());
        } else {
            for (Channel c : this.client.getChannels().values()) {
                int index = c.getUsers().indexOf(user);
                // Note: this will not change adminUsers user, but we should not be using
                // adminUsers for anything other than checking if someone is admin.

                if (index != -1) {
                    c.getUsers().get(index).setStatus(user.getStatus());
                }
            }
        }

        if (this.client.hasCurrentChannel() && this.client.getCurrentChannel().getUsers().contains(user)) {
            this.client.getMainMenuFrame().reload(event);
        }
    }
}
