package duberchat.handlers.client;

import duberchat.client.ChatClient;
import duberchat.events.ChannelDeleteEvent;
import duberchat.events.SerializableEvent;
import duberchat.handlers.Handleable;

public class ClientChannelDeleteHandler implements Handleable {
    protected ChatClient client;

    public ClientChannelDeleteHandler(ChatClient client) {
        this.client = client;
    }

    public void handleEvent(SerializableEvent event) {
        ChannelDeleteEvent deleteEvent = (ChannelDeleteEvent) event;

        this.client.getChannels().remove(deleteEvent.getChannel().getChannelId());

        if (this.client.hasCurrentChannel() && this.client.getCurrentChannel().equals(deleteEvent.getChannel())) {
            this.client.setCurrentChannel(null);
        }

        this.client.getMainMenuFrame().reload(event);
    }
}
