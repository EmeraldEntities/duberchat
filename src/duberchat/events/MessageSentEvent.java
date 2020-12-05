package duberchat.events;

import duberchat.chatutil.Message;

/**
 * A {@code MessageSentEvent} is an event that is created when a message is sent
 * to the server.
 * <p>
 * This is the most common event sent, and should be given optimization
 * priority. Sequential order is important for this event.
 * 
 * <p>
 * Since <b>2020-12-04</b>.
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Joseph Wang
 */
public class MessageSentEvent extends MessageEvent {
    static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@code MessageSentEvent}.
     * 
     * @param source  The source of this event.
     * @param message The new {@code Message}.
     */
    public MessageSentEvent(Object source, Message message) {
        super(source, message);
    }
}
