package duberchat.events;

/**
 * An {@code AuthFailedEvent} is a specific authentication event that represents
 * an authentication failed error.
 * <p>
 * This should be the class used to indicate a failed in authentication (eg.
 * username/passwords do not match, a username was already taken, etc.)
 * 
 * <p>
 * Since <b>2020-12-04</b>.
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Joseph Wang
 */
public class AuthFailedEvent extends AuthEvent {
    static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@code AuthFailedEvent}.
     * 
     * @param source The source of this event.
     */
    public AuthFailedEvent(Object source) {
        super(source);
    }
}