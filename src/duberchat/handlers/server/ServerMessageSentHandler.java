package duberchat.handlers.server;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.HashMap;

import duberchat.chatutil.*;
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
        Channel destination = toSend.getChannel();
        Channel serverDestination = server.getChannels().get(destination.getChannelId());
        int msgId = serverDestination.getTotalMessages();
        long timeStamp = toSend.getTimestamp().getTime();
        Message newMessage = new Message(msgString, senderUsername, msgId, new Date(timeStamp), 
                                         serverDestination);
        serverDestination.addMessage(newMessage);
        serverDestination.setTotalMessages(msgId + 1);
        destination.setTotalMessages(msgId + 1);   // added for clarity TODO
        try {
            // Add the new message with the appropriate file path to the file write queue.
            // Messages are formatted like this: id timestamp senderUsername msg
            String filePath = "data/channels/" + destination.getChannelId() + ".txt";
            HashMap<String, HashMap<Integer, String>> rewriteMap = new HashMap<>();
            HashMap<Integer, String> rewriteInnerMap = new HashMap<>();
            int lineNum = destination.getUsers().size() + destination.getAdminUsers().size() + 5;
            rewriteInnerMap.put(lineNum, (msgId + 1) + "\n");
            rewriteMap.put(filePath,  rewriteInnerMap);
            server.getFileRewriteQueue().add(rewriteMap);
            String[] msgArr = {filePath, msgId + " " + timeStamp + " " + senderUsername + " " + 
                                msgString + "\n"};
            server.getFileAppendQueue().add(msgArr);

            // Send back a message sent event to every online user in the channel
            for (User member : serverDestination.getUsers()) {
                // skip offline users
                if (!server.getCurUsers().containsKey(member)) continue;
                ObjectOutputStream output = server.getCurUsers().get(member).getOutputStream();
                output.writeObject(new MessageSentEvent((User) event.getSource(), newMessage));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
