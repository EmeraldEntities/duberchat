package duberchat.events;

import java.util.EventObject;

public class RequestFailedEvent extends EventObject {
    static final long serialVersionUID = 1L;

    public RequestFailedEvent(Object source) {
        super(source);
    }
}
