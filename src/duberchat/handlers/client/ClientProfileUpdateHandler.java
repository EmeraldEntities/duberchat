package duberchat.handlers.client;

import duberchat.chatutil.Channel;
import duberchat.chatutil.User;
import duberchat.client.ChatClient;
import duberchat.events.ClientProfileUpdateEvent;
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
 * @see duberchat.events.ClientProfileUpdateEvent
 */
public class ClientProfileUpdateHandler implements Handleable {
    /** The associated client this handler is attached to. */
    protected ChatClient client;

    /**
     * Constructs a new {@code ClientStatusUpdateHandler}.
     * 
     * @param client the client that this handler is attached to.
     */
    public ClientProfileUpdateHandler(ChatClient client) {
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
        ClientProfileUpdateEvent statusEvent = (ClientProfileUpdateEvent) event;
        User user = (User) statusEvent.getSource();

        // If i initialized it, the source would be from me
        if (this.client.getUser().equals(user)) {
            this.client.getUser().setStatus(user.getStatus());
        } else {
            for (Channel c : this.client.getChannels().values()) {
                User userToFix = c.getUsers().get(user.getUsername());

                if (userToFix != null && userToFix.getStatus() != user.getStatus()) {
                    userToFix.setStatus(user.getStatus());
                }
            }
        }

        if (this.client.hasCurrentChannel()
                && this.client.getCurrentChannel().getUsers().containsKey(this.client.getUser().getUsername())) {
            this.client.getMainMenuFrame().reload(event);
        }
    }
}
