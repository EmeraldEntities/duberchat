package duberchat.events;

import duberchat.chatutil.Message;

public class MessageEditEvent extends MessageEvent {
    static final long serialVersionUID = 1L;

    public MessageEditEvent(Object source, Message message) {
        super(source, message);
    }
}
