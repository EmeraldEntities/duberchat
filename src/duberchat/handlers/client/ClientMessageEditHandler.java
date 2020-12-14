package duberchat.handlers.client;

import java.util.ArrayList;

import duberchat.chatutil.Channel;
import duberchat.chatutil.Message;
import duberchat.client.ChatClient;
import duberchat.events.MessageEditEvent;
import duberchat.events.SerializableEvent;
import duberchat.handlers.Handleable;

/**
 * the {@code ClientMessageEditHandler} class provides the client-side
 * implementation for handling the {@code MessageEditEvent} sent back from the
 * server.
 * <p>
 * Created <b>2020-12-11</b>
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Joseph Wang
 * @see duberchat.events.MessageEditEvent
 */
public class ClientMessageEditHandler implements Handleable {
    /** The associated client this handler is attached to. */
    protected ChatClient client;

    /**
     * Constructs a new {@code ClientMessageEditHandler}.
     * 
     * @param client the client that this handler is attached to.
     */
    public ClientMessageEditHandler(ChatClient client) {
        this.client = client;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Ensures that the client updates any messages that were edited, and reloads
     * the text panel.
     * 
     * @param event {@inheritDoc}
     */
    public void handleEvent(SerializableEvent event) {
        MessageEditEvent msgEvent = (MessageEditEvent) event;
        Message message = msgEvent.getMessage();
        Channel localChannel = client.getChannels().get(message.getChannelId());

        // We replace the message with the new one, even if we are the ones that created
        // it. This ensures that if we don't handle it locally, nothing will
        // break or work incorrectly.
        ArrayList<Message> localMessages = localChannel.getMessages();

        for (int i = 0; i < localMessages.size(); i++) {
            if (localMessages.get(i).equals(message)) {
                // replace method because pointers are annoying
                localMessages.set(i, message);
                break;
            }
        }

        if (client.hasCurrentChannel() && client.getCurrentChannel().equals(localChannel)) {
            client.getMainMenuFrame().reload(event);
        }
    }
}
