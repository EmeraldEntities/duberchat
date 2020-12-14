package duberchat.handlers.client;

import duberchat.client.ChatClient;
import duberchat.events.ClientPasswordUpdateEvent;
import duberchat.events.SerializableEvent;
import duberchat.handlers.Handleable;

/**
 * the {@code ClientPasswordUpdateHandler} class provides the client-side
 * implementation for handling the {@code ClientPasswordUpdateEvent} sent back
 * from the server.
 * <p>
 * While the client may not use the password, this handler is important to
 * ensure sync and easy updating if the client one day needs to use their
 * password.
 * <p>
 * Created <b>2020-12-09</b>
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Joseph Wang
 * @see duberchat.events.ClientPasswordUpdateEvent
 */
public class ClientPasswordUpdateHandler implements Handleable {
    /** The associated client this handler is attached to. */
    protected ChatClient client;

    /**
     * Constructs a new {@code ClientPasswordUpdateHandler}.
     * 
     * @param client the client that this handler is attached to.
     */
    public ClientPasswordUpdateHandler(ChatClient client) {
        this.client = client;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Ensures that this user has the new password.
     * 
     * @param event {@inheritDoc}
     */
    public void handleEvent(SerializableEvent event) {
        ClientPasswordUpdateEvent passwordEvent = (ClientPasswordUpdateEvent) event;
        String username = (String) passwordEvent.getSource();
        long newHashedPassword = passwordEvent.getHashedPassword();

        // Make sure the client's user has the updated password
        if (this.client.getUser().getUsername().equals(username)) {
            this.client.getUser().setHashedPassword(newHashedPassword);

            if (this.client.getMainMenuFrame().hasActiveProfileFrame()) {
                this.client.getMainMenuFrame().getProfileFrame().reload(event);
            }
        }
    }
}
