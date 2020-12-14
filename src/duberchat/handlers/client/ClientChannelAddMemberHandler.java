package duberchat.handlers.client;

import duberchat.chatutil.User;
import duberchat.client.ChatClient;
import duberchat.events.SerializableEvent;
import duberchat.events.ChannelAddMemberEvent;
import duberchat.handlers.Handleable;

/**
 * the {@code ClientChannelAddMemberHandler} class provides the client-side
 * implementation for handling any {@code ChannelAddMemberEvent}.
 * <p>
 * <p>
 * Created <b>2020-12-12</b>
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Joseph Wang
 * @see duberchat.events.ChannelAddMemberEvent
 */
public class ClientChannelAddMemberHandler implements Handleable {
    /** The associated client this handler is attached to. */
    protected ChatClient client;

    /**
     * Constructs a new {@code ClientChannelAddMemberHandler}.
     * 
     * @param client the client that this handler is attached to.
     */
    public ClientChannelAddMemberHandler(ChatClient client) {
        this.client = client;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Ensures that the client has a properly updated user list.
     * 
     * @param event {@inheritDoc}
     */
    public void handleEvent(SerializableEvent event) {
        ChannelAddMemberEvent memberEvent = (ChannelAddMemberEvent) event;
        int newChannelId = memberEvent.getChannelId();

        if (!this.client.getChannels().containsKey(newChannelId)) {
            // This user is the new user
            System.out.println(memberEvent.getNewChannel());
            this.client.getChannels().put(newChannelId, memberEvent.getNewChannel());
            this.client.getUser().getChannels().add(newChannelId);

            this.client.getMainMenuFrame().reload(event);
        } else {
            // This user is not the new user
            User newUser = memberEvent.getNewUser();

            this.client.getChannels().get(newChannelId).getUsers().put(newUser.getUsername(), newUser);

            // We only need to reload if we are currently looking at the channel
            if (this.client.hasCurrentChannel() && this.client.getCurrentChannel().getChannelId() == newChannelId) {
                this.client.getMainMenuFrame().reload(event);
            }
        }

    }
}
