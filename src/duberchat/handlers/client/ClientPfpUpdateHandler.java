package duberchat.handlers.client;

import java.awt.image.BufferedImage;

import duberchat.chatutil.Channel;
import duberchat.client.ChatClient;
import duberchat.events.ClientPfpUpdateEvent;
import duberchat.events.SerializableEvent;
import duberchat.handlers.Handleable;

/**
 * the {@code ClientPfpUpdateHandler} class provides the client-side
 * implementation for handling the {@code ClientPfpUpdateEvent} sent back from
 * the server.
 * <p>
 * Created <b>2020-12-09</b>
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Joseph Wang
 * @see duberchat.events.ClientPfpUpdateEvent
 */
public class ClientPfpUpdateHandler implements Handleable {
    /** The associated client this handler is attached to. */
    protected ChatClient client;

    /**
     * Constructs a new {@code ClientPfpUpdateHandler}.
     * 
     * @param client the client that this handler is attached to.
     */
    public ClientPfpUpdateHandler(ChatClient client) {
        this.client = client;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Ensures that every instance of the specified user is changed to have that
     * specific profile picture.
     * 
     * @param event {@inheritDoc}
     */
    public void handleEvent(SerializableEvent event) {
        ClientPfpUpdateEvent pfpEvent = (ClientPfpUpdateEvent) event;
        String username = (String) pfpEvent.getSource();
        BufferedImage newPfp = pfpEvent.getNewPfp();
        String newPfpFormat = pfpEvent.getPfpFormat();

        // If i initialized it, the source would be from me
        // ensure current user is edited
        // Make sure the profile frame is updated
        if (this.client.getUser().getUsername().equals(username)) {
            this.client.getUser().setPfp(newPfp);
            this.client.getUser().setPfpFormat(newPfpFormat);

            if (this.client.getMainMenuFrame().hasActiveProfileFrame()) {
                this.client.getMainMenuFrame().getProfileFrame().reload(event);
            }
        }

        // Loop over every single channel and replace the user's pfp with the new one
        // Even if we're the target user, do this to ensure a good sync
        for (Channel c : this.client.getChannels().values()) {
            if (c.getUsers().containsKey(username)) {
                c.getUsers().get(username).setPfp(newPfp);
                c.getUsers().get(username).setPfpFormat(newPfpFormat);
            }
        }

        if (this.client.hasMainMenuFrame()) {
            this.client.getMainMenuFrame().reload(event);
        }
    }
}
