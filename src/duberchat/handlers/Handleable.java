package duberchat.handlers;

import duberchat.events.SerializableEvent;

public interface Handleable {
    public void handleEvent(SerializableEvent event);
}
