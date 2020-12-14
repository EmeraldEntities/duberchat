package duberchat.events;

import duberchat.chatutil.Message;

/**
 * A {@code MessageEditEvent} is an event that is created when a client edits a
 * message.
 * <p>
 * Synchronization across all clients is important for this event. A user should
 * only be able to edit their own message, admin or not.
 * <p>
 * Since <b>2020-12-04</b>.
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Joseph Wang
 */
public class MessageEditEvent extends MessageEvent {
    static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@code MessageEditEvent}.
     * 
     * @param source  The source of this event.
     * @param message The edited {@code Message}.
     */
    public MessageEditEvent(Object source, Message message) {
        super(source, message);
    }
}
