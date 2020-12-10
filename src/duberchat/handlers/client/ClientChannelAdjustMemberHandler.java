package duberchat.handlers.client;

import duberchat.chatutil.Channel;
import duberchat.client.ChatClient;
import duberchat.events.SerializableEvent;
import duberchat.events.ChannelEvent;
import duberchat.handlers.Handleable;

public class ClientChannelAdjustMemberHandler implements Handleable {
    protected ChatClient client;

    public ClientChannelAdjustMemberHandler(ChatClient client) {
        this.client = client;
    }

    public void handleEvent(SerializableEvent event) {
        ChannelEvent memberEvent = (ChannelEvent) event;
        Channel modifiedChannel = memberEvent.getChannel();

        // Make sure we never have to deal with NPE if server sends smth bad
        if (!this.client.getChannels().containsKey(modifiedChannel.getChannelId())) {
            return;
        }

        this.client.getChannels().get(modifiedChannel.getChannelId()).setUsers(modifiedChannel.getUsers());

        // We only need to reload if we are currently looking at the channel
        if (this.client.hasCurrentChannel() && this.client.getCurrentChannel().equals(modifiedChannel)) {
            this.client.getMainMenuFrame().reload(event);
        }
    }
}
