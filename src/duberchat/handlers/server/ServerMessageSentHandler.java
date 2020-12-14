package duberchat.handlers.server;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.Iterator;

import duberchat.chatutil.*;
import duberchat.events.FileWriteEvent;
import duberchat.events.MessageSentEvent;
import duberchat.events.SerializableEvent;
import duberchat.handlers.Handleable;
import duberchat.server.ChatServer;

/**
 * A {@code ServerMessageSentHandler} is a handler that processes sent messages.
 * <p>
 * The handler receives a {@code MessageSentEvent} from the server, which itself came from the 
 * client. The handler adds the message to the file writing queue and propagates the event with an  
 * updated {@code Message} object to all the currently online users in the channel.
 * 
 * <p>
 * Since <b>2020-12-06</b>.
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Paula Yuan
 */
public class ServerMessageSentHandler implements Handleable {
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
     * Handles the {@code MessageSentEvent}.
     * 
     * @param newEvent The event to be handled. 
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

        Message newMessage = new Message(processedMsg, senderUsername, msgId, timeStamp, destinationId);
        destination.getMessages().add(newMessage);
        destination.setTotalMessages(msgId + 1);
        try {
            // Send back a message sent event to every online user in the channel
            Iterator<User> itr = destination.getUsers().values().iterator();
            while (itr.hasNext()) {
                User member = itr.next(); 
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
