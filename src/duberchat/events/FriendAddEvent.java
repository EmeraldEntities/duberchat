package duberchat.events;

/**
 * A {@code FriendAddEvent} is an event that is created when a friend is added to a user.
 * 
 * Since <b>2020-12-12</b>.
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Paula Yuan
 */
public class FriendAddEvent extends FriendEvent {
    static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@code FriendAddEvent}.
     * 
     * @param source  The source of this event.
     * @param friendUsername The associated friend's username.
     */
    public FriendAddEvent(Object source, String friendUsername) {
        super(source, friendUsername);
    }
}
