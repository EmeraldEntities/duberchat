package duberchat.handlers.client;

import duberchat.chatutil.Channel;
import duberchat.handlers.Handleable;
import duberchat.client.ChatClient;
import duberchat.events.ChannelCreateEvent;
import duberchat.events.SerializableEvent;

public class ClientChannelCreateHandler implements Handleable {
    protected ChatClient client;

    public ClientChannelCreateHandler(ChatClient client) {
        this.client = client;
    }

    public void handleEvent(SerializableEvent event) {
        ChannelCreateEvent channelEvent = (ChannelCreateEvent) event;

        if (this.client.getMainMenuFrame().hasActiveChannelCreateFrame()) {
            this.client.getMainMenuFrame().closeChannelCreateFrame();
        }

        Channel newChannel = channelEvent.getChannel();
        int newChannelId = newChannel.getChannelId();

        this.client.getChannels().put(newChannelId, newChannel);
        // No need to access hashmap as this channel should be a reference to the same
        // object
        this.client.setCurrentChannel(newChannel);

        // Force a refresh of the main menu frame
        this.client.getMainMenuFrame().reload();
    }
}
