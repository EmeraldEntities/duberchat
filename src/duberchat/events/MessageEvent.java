package duberchat.events;

import duberchat.chatutil.Message;

/**
 * A {@code ChannelEvent} is a generic, abstract event that represents any
 * action involving a message.
 * <p>
 * This includes events like sending a message, deleting a message, etc. This
 * also means that events of this type will be the most common, and should be
 * given priority when it comes to optimization.
 * 
 * <p>
 * Since <b>2020-12-04</b>.
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Joseph Wang
 * @see duberchat.chatutil.Message
 */
public abstract class MessageEvent extends SerializableEvent {
    static final long serialVersionUID = 1L;

    protected Message message;

    /**
     * Constructs a new {@code MessageEvent}.
     * 
     * @param source  The source of this event.
     * @param message The message associated.
     */
    public MessageEvent(Object source, Message message) {
        super(source);

        this.message = message;
    }

    /**
     * Retrieves the message associated with this event.
     * 
     * @return the {@code Message} object in this event.
     */
    public Message getMessage() {
        return this.message;
    }
}
