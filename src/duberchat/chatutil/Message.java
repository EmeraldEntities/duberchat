package duberchat.chatutil;

import java.io.Serializable;
import java.util.Date;

public class Message implements Serializable {
    static final long serialVersionUID = 2L;
    public static final int MAX_LENGTH = 100;

    private String senderUsername;
    private String message;
    private int messageId;
    private String timestamp;
    private int associatedChannelId;

    public Message(String message, String senderUsername, int messageId, String timestamp, 
                   int channelId) {
        this.senderUsername = senderUsername;
        this.message = message;
        this.messageId = messageId;
        this.timestamp = timestamp;
        this.associatedChannelId = channelId;
    }

    public Message(Message msg) {
        this.senderUsername = msg.getSenderUsername();
        this.message = msg.getMessage();
        this.messageId = msg.getMessageId();
        this.timestamp = msg.getTimestamp();
        this.associatedChannelId = msg.getChannelId();
    }

    /**
     * Custom equals method because we need to know if the values are equal, not the references.
     * 
     * @param obj The other user we're checking equality with.
     * @return boolean, whether the two users are the same (based on id)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } 
        if (obj == null || !(obj instanceof Message)) {
            return false;
        }
        Message message = (Message) obj;
        return (this.messageId == message.getMessageId());
    }

    public String getSenderUsername() {
        return this.senderUsername;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getMessageId() {
        return this.messageId;
    }

    public String getTimestamp() {
        return this.timestamp;
    }

    public int getChannelId() {
        return this.associatedChannelId;
    }
}
