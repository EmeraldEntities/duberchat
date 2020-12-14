package duberchat.handlers.server;

import java.io.IOException;
import java.io.ObjectOutputStream;

import duberchat.chatutil.*;
import duberchat.events.FileWriteEvent;
import duberchat.events.MessageSentEvent;
import duberchat.events.SerializableEvent;
import duberchat.handlers.Handleable;
import duberchat.server.ChatServer;

/**
 * the {@code ServerMessageSentHandler} class provides the server-side
 * implementation for handling any {@code MessageSentEvent}.
 * <p>
 * <p>
 * Created <b>2020-12-06</b>
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Paula Yuan
 * @see duberchat.events.ChannelMessageSentEvent
 */
public class ServerMessageSentHandler implements Handleable {
    /** The associated server this handler is attached to. */
    private ChatServer server;

    /**
     * Constructs a new {@code ServerMessageSentHandler}.
     * 
     * @param server The associated server with this handler.
     */
    public ServerMessageSentHandler(ChatServer server) {
        this.server = server;
    }

  /**
   * {@inheritDoc}
   * <p>
   * Ensures that the server has a properly channel, updates files, and sends the
   * event to all relevant users.
   * 
   * @param newEvent {@inheritDoc}
   */
    public void handleEvent(SerializableEvent newEvent) {
        MessageSentEvent event = (MessageSentEvent) newEvent;
        Message toSend = event.getMessage();
        String msgString = toSend.getMessage();
        String senderUsername = toSend.getSenderUsername();
        int destinationId = toSend.getChannelId();
        Channel destination = server.getChannels().get(destinationId);
        int msgId = destination.getTotalMessages();
        String timeStamp = toSend.getTimestamp();

        // Process text conversions and emoticon conversions
        String[] words = msgString.split(" ");
        String processedMsg = "";
        for (String word : words) {
            if (server.getTextConversions().containsKey(word)) {
                word = server.getTextConversions().get(word);
            }
            processedMsg += word + " ";
        }
        processedMsg.trim();

        // update the server-side channel with the new message
        Message newMessage = new Message(processedMsg, senderUsername, msgId, timeStamp, destinationId);
        destination.getMessages().add(newMessage);
        destination.setTotalMessages(msgId + 1);

        try {
            // Send back a message sent event to every online user in the channel
            for (User member : destination.getUsers().values()) {
                // skip offline users
                if (!server.getCurUsers().containsKey(member)) continue;
                ObjectOutputStream output = server.getCurUsers().get(member).getOutputStream();
                output.writeObject(new MessageSentEvent(event.getSource(), newMessage));
                output.flush();
                output.reset();
            }
            server.getServerFrame().getTextArea()
                    .append("New message sent to channel " + destinationId + "and events sent to users\n");

            // Update the channel file.
            String filePath = "data/channels/" + destination.getChannelId();
            server.getFileWriteQueue().add(new FileWriteEvent(destination, filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
