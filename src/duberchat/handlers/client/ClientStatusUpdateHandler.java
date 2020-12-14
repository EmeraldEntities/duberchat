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
     * specific status.
     * 
     * @param event {@inheritDoc}
     */
    public void handleEvent(SerializableEvent event) {
        ClientStatusUpdateEvent statusEvent = (ClientStatusUpdateEvent) event;
        String username = (String) statusEvent.getSource();
        int newStatus = statusEvent.getStatus();

        // If i initialized it, the source would be from me
        // ensure current user is edited
        // Make sure the profile frame is updated
        if (this.client.getUser().getUsername().equals(username)) {
            this.client.getUser().setStatus(newStatus);

            if (this.client.getMainMenuFrame().hasActiveProfileFrame()) {
                this.client.getMainMenuFrame().getProfileFrame().reload(event);
            }
        }

        // Loop over every single channel and replace the user's status with the new one
        // Even if we're the target user, do this to ensure a good sync
        for (Channel c : this.client.getChannels().values()) {
            if (c.getUsers().containsKey(username)) {
                c.getUsers().get(username).setStatus(newStatus);
            }
        }

        for (User f : this.client.getFriends().values()) {
            if (f.getUsername().equals(username)) {
                f.setStatus(newStatus);
            }
        }

        if (this.client.hasMainMenuFrame()) {
            this.client.getMainMenuFrame().reload(event);
        }
    }
}
