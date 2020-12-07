package duberchat.handlers.server;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Date;

import duberchat.chatutil.*;
import duberchat.events.MessageSentEvent;
import duberchat.events.SerializableEvent;
import duberchat.handlers.Handleable;
import duberchat.server.ChatServer;

public class ServerMessageSentHandler implements Handleable {
    private ChatServer server;

    public ServerMessageSentHandler(ChatServer server) {
        this.server = server;
    }

    public void handleEvent(SerializableEvent newEvent) {
        MessageSentEvent event = (MessageSentEvent) newEvent;
        Message toSend = event.getMessage();
        String msgString = toSend.getMessage();
        String senderUsername = toSend.getSenderUsername();
        Channel destination = toSend.getChannel();
        int msgId = destination.getMessages().size();
        long timeStamp = toSend.getTimestamp().getTime();
        Channel serverDestinationChannel = server.getChannels().get(destination.getChannelId());
        Message newMessage = new Message(msgString, senderUsername, msgId, new Date(timeStamp), 
                                         serverDestinationChannel);
        serverDestinationChannel.addMessage(newMessage);
        try {
            // Add the new message with the appropriate file path to the file write queue.
            // Messages are formatted like this: id tismeStamp senderUsername msg
            String[] msgArr = {"data/channels/" + destination.getChannelId() + ".txt",
                                msgId + " " + timeStamp + " " + senderUsername + " " + 
                                msgString + "\n"};
            server.getFileWriteQueue().add(msgArr);

            // Send back a message sent event to every online user in the channel
            for (User member : serverDestinationChannel.getUsers()) {
                if (!server.getCurUsers().containsKey(member)) continue;
                ObjectOutputStream output = server.getCurUsers().get(member).getOutputStream();
                output.writeObject(new MessageSentEvent((User) event.getSource(), newMessage));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
