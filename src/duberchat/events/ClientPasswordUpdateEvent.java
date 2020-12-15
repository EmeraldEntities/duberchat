package duberchat.events;

/**
 * A {@code ClientPasswordUpdateEvent} is an event that is created when a client
 * updates their password
 * 
 * <p>
 * Since <b>2020-12-13</b>.
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Joseph Wang, Paula Yuan
 */
public class ClientPasswordUpdateEvent extends ClientProfileUpdateEvent {
    static final long serialVersionUID = 1L;

    /** The hashed password associated with this event. */
    protected long hashedPassword;

    /**
     * Constructs a new {@code ClientPasswordUpdateEvent}.
     * 
     * @param source         the source of this event.
     * @param hashedPassword the new hashed password.
     */
    public ClientPasswordUpdateEvent(Object source, long hashedPassword) {
        super(source);

        this.hashedPassword = hashedPassword;
    }

    /**
     * Constructs a new {@code ClientPasswordUpdateEvent}.
     * 
     * @param source   the source of this event.
     * @param password the new password, as a string.
     */
    public ClientPasswordUpdateEvent(Object source, String password) {
        super(source);

        this.hashedPassword = password.hashCode();
    }

    /**
     * Retrieves the new password associated with this event.
     * 
     * @return a {@code long} representing the user's new password, hashed.
     */
    public long getHashedPassword() {
        return this.hashedPassword;
    }

    /**
     * Assigns the new password associated with this event.
     * 
     * @param hashedPassword, a {@code long} representing the user's new password, hashed.
     */
    public void setHashedPassword(long hashedPassword) {
        this.hashedPassword = hashedPassword;
    }
}