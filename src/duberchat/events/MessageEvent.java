package duberchat.events;

import java.util.EventObject;
import duberchat.chatutil.Message;

public abstract class MessageEvent extends EventObject {
    static final long serialVersionUID = 1L;

    protected Message message;

    public MessageEvent(Object source, Message message) {
        super(source);

        this.message = message;
    }

    public Message getMessage() {
        return this.message;
    }
}
