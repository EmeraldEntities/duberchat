package duberchat.events;

import duberchat.chatutil.Message;

public class MessageDeleteEvent extends MessageEvent {
    static final long serialVersionUID = 1L;

    public MessageDeleteEvent(Object source, Message message) {
        super(source, message);
    }
}
