package duberchat.events;

import duberchat.chatutil.User;
import duberchat.chatutil.Channel;

import java.util.HashMap;

/**
 * An {@code AuthSucceedEvent} is a specific authentication event that
 * represents an authentication success.
 * <p>
 * This event should be used solely to indicate a success in authentication,
 * where the requesting client is able to retrieve the metadata stored in this
 * event and immediately begin normal operations.
 * 
 * <p>
 * Since <b>2020-12-04</b>.
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Joseph Wang, Paula Yuan
 */
public class AuthSucceedEvent extends AuthEvent {
    static final long serialVersionUID = 1L;

    protected User user;
    protected HashMap<Integer, Channel> channels;
    protected HashMap<String, User> friends;

    /**
     * Constructs a new {@code AuthSucceedEvent}
     * 
     * @param source   The source of this event.
     * @param user     The returned {@code User} object for the requested client.
     * @param channels The channels that the specified User is in.
     * @param friends The friends that this User has.
     * @see duberchat.chatutil.User
     * @see duberchat.chatutil.Channel
     */
    public AuthSucceedEvent(Object source, User user, HashMap<Integer, Channel> channels, HashMap<String, User> friends) {
        super(source);

        this.user = user;
        this.channels = channels;
        this.friends = friends;
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

    /**
     * Retrieves the friends the client has.
     * 
     * @return a {@code HashMap} of username to {@code User} objects
     *         representing the friends the client has.
     */
    public HashMap<String, User> getFriends() {
        return this.friends;
    }
}
