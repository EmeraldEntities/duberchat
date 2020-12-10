package duberchat.handlers.client;

import duberchat.chatutil.Channel;
import duberchat.chatutil.Message;
import duberchat.client.ChatClient;
import duberchat.events.MessageSentEvent;
import duberchat.events.SerializableEvent;
import duberchat.handlers.Handleable;

public class ClientMessageSentHandler implements Handleable {
    protected ChatClient client;

    public ClientMessageSentHandler(ChatClient client) {
        this.client = client;
    }

    public void handleEvent(SerializableEvent event) {
        MessageSentEvent msgEvent = (MessageSentEvent) event;
        Message message = msgEvent.getMessage();

        Channel localChannel = client.getChannels().get(message.getChannel().getChannelId());

        localChannel.addMessage(message);

        if (client.hasCurrentChannel() && client.getCurrentChannel().equals(localChannel)) {
            client.getMainMenuFrame().reload(event);
        }
    }
}
