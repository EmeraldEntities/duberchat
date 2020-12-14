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
 * Since <b>2020-12-13</b>.
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Joseph Wang, Paula Yuan
 */
public class ClientStatusUpdateEvent extends ClientEvent {
    static final long serialVersionUID = 1L;
    protected int status;

    /**
     * Constructs a new {@code ClientStatusUpdateEvent}.
     * 
     * @param source The source of this event.
     */
    public ClientStatusUpdateEvent(Object source, int status) {
        super(source);

        this.status = status;
    }

    public int getStatus() {
        return this.status;
    }
}