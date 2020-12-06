package duberchat.handlers;

import duberchat.events.SerializableEvent;

/**
 * The {@code Handleable} interface is designed to be used with classes that are
 * able to handle specific events.
 * <p>
 * This interface allows for easy use of an association map to call a specific
 * event handler for custom events.
 * <p>
 * Created <b>2020-12-04</b>
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Joseph Wang
 */
public interface Handleable {
    /**
     * Handles and either consumes or propagates a specified event appropriately
     * according to this handler's implementation.
     * 
     * @param event the specific event to handle.
     */
    public void handleEvent(SerializableEvent event);
}
