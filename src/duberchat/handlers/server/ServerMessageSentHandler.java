package duberchat.handlers.server;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.HashMap;

import duberchat.chatutil.*;
import duberchat.events.MessageSentEvent;
import duberchat.events.SerializableEvent;
import duberchat.handlers.Handleable;
import duberchat.server.ChatServer.ConnectionHandler;

public class ServerMessageSentHandler implements Handleable {
    private HashMap<Integer, Channel> serverChannels;
    private HashMap<User, ConnectionHandler> onlineUsers;

    public ServerMessageSentHandler(HashMap<Integer, Channel> serverChannels, 
                                    HashMap<User, ConnectionHandler> onlineUsers) {
        this.serverChannels = serverChannels;
        this.onlineUsers = onlineUsers;
    }

    public void handleEvent(SerializableEvent newEvent) {
        MessageSentEvent event = (MessageSentEvent) newEvent;
        Message toSend = event.getMessage();
        String msgString = toSend.getMessage();
        String senderUsername = toSend.getSenderUsername();
        Channel destination = toSend.getChannel();
        int msgId = destination.getMessages().size();
        long timeStamp = toSend.getTimestamp().getTime();
        Channel serverDestinationChannel = serverChannels.get(destination.getChannelId());
        Message newMessage = new Message(msgString, senderUsername, msgId, new Date(timeStamp), 
                                         serverDestinationChannel);
        serverDestinationChannel.addMessage(newMessage);
        File channelFile = new File("data/channels/" + destination.getChannelId() + ".txt");
        try {
            // If the channel doesn't exist, something is very wrong.
            if (!channelFile.exists()) {
                // TODO: should i throw an exception here or somethign?
                System.out.println("go check that destination");
                return;
            }

            // Write the new message to the channel file.
            // Messages are formatted like this: id timeStamp senderUsername msg
            FileWriter writer = new FileWriter(channelFile, true);
            writer.write(msgId + " " + timeStamp + " " + senderUsername + " " + msgString + "\n");
            writer.close();

            // Send back a message sent event to every online user in the channel
            for (User member : serverDestinationChannel.getUsers()) {
                if (!onlineUsers.containsKey(member)) continue;
                ObjectOutputStream output = onlineUsers.get(member).getOutputStream();
                output.writeObject(new MessageSentEvent((User) event.getSource(), newMessage));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
