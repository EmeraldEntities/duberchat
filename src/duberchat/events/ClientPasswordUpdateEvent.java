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
public class ClientPasswordUpdateEvent extends ClientEvent {
    static final long serialVersionUID = 1L;

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

    public long getHashedPassword() {
        return this.hashedPassword;
    }

    public void setHashedPassword(long hashedPassword) {
        this.hashedPassword = hashedPassword;
    }
}