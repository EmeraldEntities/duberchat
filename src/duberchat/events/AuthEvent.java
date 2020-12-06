package duberchat.events;

/**
 * An {@code AuthEvent} is a generic, abstract class that represents an
 * authentication event sent by the server.
 * <p>
 * The client should never send {@code AuthEvent}, as the server is the only
 * object that should be authenticating clients. As a direct result, the server
 * does not need to implement any handlers for this event.
 * 
 * <p>
 * Since <b>2020-12-04</b>.
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Joseph Wang
 */
public abstract class AuthEvent extends SerializableEvent {
    static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@code AuthEvent}.
     * 
     * @param source The source of this event.
     */
    public AuthEvent(Object source) {
        super(source);
    }
}
