package duberchat.events;

import duberchat.chatutil.Channel;

/**
 * A {@code ChannelRemoveMemberEvent} is an event that is created when a user
 * should be removed from a channel.
 * <p>
 * The client should send this event when a user is removed from a channel. The
 * server will update and propagate the event (with an updated {@code Channel}
 * object) to the other clients, allowing for proper updating of channel
 * members.
 * <p>
 * Only admin users should be able to create this event.
 * 
 * <p>
 * Since <b>2020-12-04</b>.
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Joseph Wang, Paula Yuan
 */
public class ChannelRemoveMemberEvent extends ChannelEvent {
    static final long serialVersionUID = 1L;
    private String username;

    /**
     * Constructs a new {@code ChannelRemoveMemberEvent}.
     * 
     * @param source    The source of this event.
     * @param channelId The associated channel's id with this event.
     * @param username  The username of the user to remove.
     */
    public ChannelRemoveMemberEvent(Object source, int channelId, String username) {
        super(source, channelId);
        this.username = username;
    }

    /**
     * Retrieves the {@code String} username of the user to remove.
     * 
     * @returns String, the username of the user to remove.
     */
    public String getUsername() {
        return this.username;
    }
}
