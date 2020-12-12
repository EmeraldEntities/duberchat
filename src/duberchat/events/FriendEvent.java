package duberchat.events;

/**
 * A {@code FriendEvent} is a generic event that represents any action involving a friend.
 * <p>
 * This includes events like adding and removing a friend.
 * 
 * <p>
 * Since <b>2020-12-12</b>.
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Paula Yuan
 */
public class FriendEvent extends SerializableEvent {
    static final long serialVersionUID = 1L;

    protected String friendUsername;

    /**
     * Constructs a new {@code FriendEventj}.
     * 
     * @param source  The source of this event.
     * @param friendUsername The username of the friend associated.
     */
    public FriendEvent(Object source, String friendUsername) {
        super(source);

        this.friendUsername = friendUsername;
    }

    /**
     * Retrieves the friend username associated with this event.
     * 
     * @return the username of the friend
     */
    public String getFriendUsername() {
        return this.friendUsername;
    }
}