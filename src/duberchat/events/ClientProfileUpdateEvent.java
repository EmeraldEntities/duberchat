package duberchat.events;

/**
 * A {@code ClientProfileUpdateEvent} is an event that is created when a client
 * updates their profile in any way.
 * 
 * <p>
 * Since <b>2020-12-04</b>.
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Joseph Wang, Paula Yuan
 */
public class ClientProfileUpdateEvent extends ClientEvent {
    static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@code ClientStatusUpdateEvent}.
     * 
     * @param source The source of this event.
     */
    public ClientProfileUpdateEvent(Object source) {
        super(source);
    }
}