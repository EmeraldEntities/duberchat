package duberchat.events;

import duberchat.chatutil.Message;
import duberchat.chatutil.User;
import duberchat.chatutil.Channel;

import java.util.HashMap;
import java.util.ArrayList;

/**
 * An {@code AuthSucceedEvent} is a specific authentication event that
 * represents an authentication success.
 * <p>
 * This event should be used solely to indicate a success in authentication,
 * where the requesting client is able to retrieve the metadata stored in this
 * event and immedietely begin normal operations.
 * 
 * <p>
 * Since <b>2020-12-04</b>.
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Joseph Wang
 */
public class AuthSucceedEvent extends AuthEvent {
    static final long serialVersionUID = 1L;

    protected User user;
    protected HashMap<Integer, Channel> channels;
    protected ArrayList<Message> recentChannelMessages;

    /**
     * Constructs a new {@code AuthSucceedEvent}
     * 
     * @param source   The source of this event.
     * @param user     The returned {@code User} object for the requested client.
     * @param channels The channels that the specified User is in.
     * @see duberchat.chatutil.User
     * @see duberchat.chatutil.Channel
     */
    public AuthSucceedEvent(Object source, User user, HashMap<Integer, Channel> channels) {
        super(source);

        this.user = user;
        this.channels = channels;
    }

    /**
     * Retrieves the user of the client.
     * 
     * @return a {@code User} object with the User of the client.
     */
    public User getUser() {
        return this.user;
    }

    /**
     * Retrieves the channels the client is in.
     * 
     * @return a {@code HashMap} of channelID to {@code Channel} objects
     *         representing the channels the client is in.
     */
    public HashMap<Integer, Channel> getChannels() {
        return this.channels;
    }
}
