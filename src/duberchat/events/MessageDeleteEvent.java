package duberchat.events;

import duberchat.chatutil.Message;

/**
 * A {@code MessageDeleteEvent} is an event that is created when a client
 * deletes a message.
 * <p>
 * The user should only be able to delete messages that they sent, unless they
 * are an admin user, in which case they are able to delete messages from
 * anyone. Synchronization across all clients is important for this event.
 * <p>
 * Since <b>2020-12-04</b>.
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Joseph Wang
 */
public class MessageDeleteEvent extends MessageEvent {
    static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@code MessageDeleteEvent}.
     * 
     * @param source  The source of this event.
     * @param message The deleted {@code Message}.
     */
    public MessageDeleteEvent(Object source, Message message) {
        super(source, message);
    }
}
