package duberchat.events;

import java.util.EventObject;

/**
 * A {@code RequestFailedEvent} is a generic event that represents a failure
 * that cannot be classified as an existing event, or if a small and generic
 * failure must be sent to the client(s).
 * 
 * <p>
 * Since the only item passed through between client and server are objects
 * (specifically objects that extend {@link java.util.EventObject EventObject}),
 * this event serves as a bridge between primitive request confirmation packets
 * and full-fledged specific events.
 * 
 * <p>
 * Since <b>2020-12-04</b>.
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Joseph Wang
 */
public class RequestFailedEvent extends EventObject {
    static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@code RequestFailedEvent}.
     * 
     * @param source The source of this event.
     */
    public RequestFailedEvent(Object source) {
        super(source);
    }
}
