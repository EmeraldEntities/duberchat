package duberchat.chatutil;

import java.io.Serializable;

public class Message implements Serializable {
    static final long serialVersionUID = 1L;

    private String senderUsername;
    private String message;
    private int messageId;
    private Channel associatedChannel;

    public Message(String message, String senderUsername, int messageId, Channel channel) {
        this.senderUsername = senderUsername;
        this.message = message;
        this.messageId = messageId;
        this.associatedChannel = channel;
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

    public Channel getChannel() {
        return this.associatedChannel;
    }
}
