package duberchat.events;

/**
 * A {@code ClientStatusUpdateEvent} is an event that is created when a client
 * updates their client status.
 * <p>
 * This could be from a manual change to a supported status, or as a result of a
 * log-out procedure. The server should ensure that other online clients will
 * view this client as offline.
 * 
 * <p>
 * Since <b>2020-12-04</b>.
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Joseph Wang
 */
public class ClientStatusUpdateEvent extends ClientEvent {
    static final long serialVersionUID = 1L;

    protected int status;

    /**
     * Constructs a new {@code ClientStatusUpdateEvent}.
     * 
     * @param source The source of this event.
     * @param status The new status of this client, represented by a user constant.
     * @see duberchat.chatutils.User
     */
    public ClientStatusUpdateEvent(Object source, int status) {
        super(source);

        this.status = status;
    }

    /**
     * Retrieves the new status of the sending client.
     * <p>
     * The status should be a user constant, as defined in
     * {@link duberchat.chatutils.User the User class}.
     * 
     * @return the new status of the associated client's user.
     */
    public int getStatus() {
        return this.status;
    }
}
