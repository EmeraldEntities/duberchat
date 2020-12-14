package duberchat.events;

import java.util.HashSet;

import duberchat.chatutil.Channel;

/**
 * A {@code ChannelCreateEvent} is an event that is created when a new channel
 * is created.
 * <p>
 * The server is the one creating new channels and then propagating a new event
 * with the proper {@code Channel} object. As a direct result (and solution), if
 * the client is calling this event, then the associated channel is
 * {@code null}.
 * 
 * <p>
 * Since <b>2020-12-04</b>.
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Joseph Wang
 */
public class ChannelCreateEvent extends ChannelEvent {
    static final long serialVersionUID = 1L;
    protected String channelName;
    protected HashSet<String> usernames;
    protected Channel newChannel;

    /**
     * Constructs a new {@code ChannelCreateEvent}.
     * 
     * @param source      The source of this event.
     * @param channelId   The channel id associated with this event.
     * @param channelName The name of the channel.
     * @param usernames   The usernames of the users to add initially.
     * @param newChannel  The new channel that was created.
     */
    public ChannelCreateEvent(Object source, int channelId, String channelName, HashSet<String> usernames,
            Channel newChannel) {
        super(source, channelId);

        this.channelName = channelName;
        this.usernames = usernames;
        this.newChannel = newChannel;
    }

    /**
     * Constructs a new {@code ChannelCreateEvent}.
     * <p>
     * This will create a DM with the specified user.
     * 
     * @param source      The source of this event.
     * @param channelId   The channel id associated with this event.
     * @param channelName The name of the channel.
     * @param username    The username of the user to add.
     * @param newChannel  The new channel that was created.
     */
    public ChannelCreateEvent(Object source, int channelId, String channelName, String username, Channel newChannel) {
        super(source, channelId);

        this.channelName = channelName;
        this.usernames = new HashSet<String>();
        this.usernames.add(username);
        this.newChannel = newChannel;
    }

    public String getChannelName() {
        return this.channelName;
    }

    /**
     * Retrieves the usernames to add to the channel.
     * 
     * @return a {@code HashSet} with the usernames of the users to add.
     */
    public HashSet<String> getUsernames() {
        return this.usernames;
    }

    public Channel getNewChannel() {
        return this.newChannel;
    }
}
