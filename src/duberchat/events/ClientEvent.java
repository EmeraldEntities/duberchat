package duberchat.events;

/**
 * A {@code ClientEvent} is a generic, abstract event that represents any event
 * that involves modification of the sending client, a generic action the client
 * requests, or a generic request that is only applicable to one user.
 * <p>
 * If an event is more closely related to another generic event category, but
 * only applies to one client, the event should be listed as a
 * {@code ClientEvent} to indicate that this event is related to one specific
 * client (eg. {@link duberchat.events.ClientRequestMessageEvent
 * ClientRequestMessageEvent})
 * 
 * <p>
 * Since <b>2020-12-04</b>.
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Joseph Wang
 */
public abstract class ClientEvent extends SerializableEvent {
    static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@ClientEvent}.
     * 
     * @param source The source of this event.
     */
    public ClientEvent(Object source) {
        super(source);
    }
}
