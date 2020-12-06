package duberchat.handlers.client;

import duberchat.client.ChatClient;

import duberchat.handlers.Handleable;

import duberchat.events.SerializableEvent;
import duberchat.events.RequestFailedEvent;
import duberchat.events.ChannelCreateEvent;

public class ClientRequestFailedHandler implements Handleable {
    protected ChatClient client;

    public ClientRequestFailedHandler(ChatClient client) {
        this.client = client;
    }

    public void handleEvent(SerializableEvent event) {
        RequestFailedEvent failedEvent = (RequestFailedEvent) event;

        // Something went wrong with the channel creation
        if (failedEvent.getSource() instanceof ChannelCreateEvent) {
            if (this.client.getMainMenuFrame().hasActiveChannelCreateFrame()) {
                this.client.getMainMenuFrame().getChannelCreateFrame().reload();
            }
        }
    }
}
