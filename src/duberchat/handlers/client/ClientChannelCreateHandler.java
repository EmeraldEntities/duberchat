package duberchat.handlers.client;

import duberchat.chatutil.Channel;
import duberchat.chatutil.User;
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
        boolean isCreator = this.client.getUser().equals(((User) channelEvent.getSource()));

        if (isCreator && this.client.getMainMenuFrame().hasActiveChannelCreateFrame()) {
            this.client.getMainMenuFrame().closeChannelCreateFrame();
        }

        Channel newChannel = channelEvent.getChannel();
        int newChannelId = newChannel.getChannelId();

        // Make sure if we already have this channel that we don't add a secondary
        // channel.
        if (!this.client.getChannels().containsKey(newChannel.getChannelId())) {
            this.client.getChannels().put(newChannelId, newChannel);
        }

        // Make sure other users don't get forcefully pulled into the new
        // channel if they weren't the creator.
        if (isCreator) {
            // No need to access hashmap as this channel should be a reference to the same
            // object
            this.client.setCurrentChannel(newChannel);
        }
        
        // Force a refresh of the main menu frame
        this.client.getMainMenuFrame().reload(event);
    }
}
