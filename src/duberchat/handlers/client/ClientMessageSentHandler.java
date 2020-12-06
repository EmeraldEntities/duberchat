package duberchat.handlers.client;

import duberchat.client.ChatClient;
import duberchat.events.SerializableEvent;
import duberchat.handlers.Handleable;

public class ClientMessageSentHandler implements Handleable {
    protected ChatClient client;

    public ClientMessageSentHandler(ChatClient client) {
        this.client = client;
    }

    public void handleEvent(SerializableEvent event) {

    }
}
