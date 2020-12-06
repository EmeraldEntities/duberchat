package duberchat.events;

/**
 * <p>
 * The root class from which all event state objects shall be derived.
 * <p>
 * All Events are constructed with a reference to the object, the "source", that
 * is logically deemed to be the object upon which the Event in question
 * initially occurred upon. As opposed to the generic java EventObject, this event
 * is fully serializable (including the source), and thus the source is expected to
 * be serializable, as well as any inheriting subclasses.
 * <p>
 * created <b>2020-12-05</b>
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Paula Yuan
 */
public class SerializableEvent implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The object on which the Event initially occurred.
     */
    protected Object source;

    /**
     * Constructs a {@code SerializableEvent}.
     *
     * @param source the object on which the Event initially occurred.
     */
    public SerializableEvent(Object source) {
        this.source = source;
    }

    /**
     * The object on which the Event initially occurred.
     *
     * @return the object on which the Event initially occurred.
     */
    public Object getSource() {
        return source;
    }

    /**
     * Returns a String representation of this SerializableEvent.
     *
     * @return a String representation of this SerializableEvent.
     */
    public String toString() {
        return getClass().getName() + "[source=" + source + "]";
    }
}

