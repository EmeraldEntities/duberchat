package duberchat.events;

import duberchat.chatutil.Channel;
import duberchat.chatutil.User;

/**
 * A {@code ChannelAddMemberEvent} is an event that is created when a channel needs to
 * add a new user.
 * <p>
 * The client should send this event when a user is added to a channel. The
 * server will update and propagate the event (with an updated {@code Channel}
 * object) to the other clients, allowing for proper updating of channel
 * members.
 * 
 * <p>
 * Since <b>2020-12-04</b>.
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Joseph Wang, Paula Yuan
 */
public class ChannelAddMemberEvent extends ChannelEvent {
    static final long serialVersionUID = 1L;
    protected String newUserUsername;
    protected User newUser;
    protected Channel newChannel;
    
    /**
     * Constructs a new {@code ChannelAddMemberEvent}.
     * 
     * @param source          The source of this event.
     * @param channelId       The associated channel's id with this event.
     * @param newUserUsername the new user's username.
     * @param newUser         the new user.
     * @param newChannel      the new channel.
     */
    public ChannelAddMemberEvent(Object source, int channelId, String newUserUsername, User newUser,
            Channel newChannel) {
        super(source, channelId);
        this.newUserUsername = newUserUsername;
        this.newUser = newUser;
    }

    /**
     * Constructs a new {@code ChannelAddMemberEvent}.
     * 
     * @param source          The source of this event.
     * @param channelId       The associated channel's id with this event.
     * @param newUserUsername the new user's username.
     * @param newUser         the new user.
     * @param newChannel      the new channel.
     */
    public ChannelAddMemberEvent(Object source, int channelId, String newUserUsername) {
        super(source, channelId);
        this.newUserUsername = newUserUsername;
    }

    public String getNewUserUsername() {
        return this.newUserUsername;
    }

    public User getNewUser() {
        return this.newUser;
    }

    public Channel getNewChannel() {
        return this.newChannel;
    }
}
