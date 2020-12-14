package duberchat.chatutil;

import java.io.Serializable;

/**
 * A {@code Message} is an object representing a message sent to a channel, with
 * all related information.
 * <p>
 * Each channel acquires messages as they are sent by users. Messages can be
 * edited or deleted by users.
 * 
 * <p>
 * Since <b>2020-12-04</b>.
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Joseph Wang, Paula Yuan
 */
public class Message implements Serializable {
    static final long serialVersionUID = 2L;
    /** An int constant representing the maximum length for a message. */
    public static final int MAX_LENGTH = 100;

    /** A string representing the username of the user who sent this message. */
    private String senderUsername;
    /** A string representing the actual message that is being conveyed. */
    private String message;
    /** An int identifier for the message; every message has an unique id. */
    private int messageId;
    /** A string representing the date/time when this message was sent. */
    private String timestamp;
    /** An int identifier for the channel to which this message was sent. */
    private int associatedChannelId;

    /**
     * Constructor for a message.
     * 
     * @param message The actual words being conveyed by the message.
     * @param senderUsername The username of the sender.
     * @param messageId The id number of this message.
     * @param timestamp A representation of when this message was sent.
     * @param channelId The id number of the channel this message is in.
     */
    public Message(String message, String senderUsername, int messageId, String timestamp, 
                   int channelId) {
        this.senderUsername = senderUsername;
        this.message = message;
        this.messageId = messageId;
        this.timestamp = timestamp;
        this.associatedChannelId = channelId;
    }

    /**
     * Constructor for a message that makes a copy of another message.
     * 
     * @param msg The {@code Message} object to copy from.
     */
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
     * @param obj The other message we're checking equality with.
     * @return boolean, whether the two messages are the same (based on id)
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

    /**
     * Retrieves the message sender's username.
     * 
     * @return String, the username.
     */
    public String getSenderUsername() {
        return this.senderUsername;
    }

    /**
     * Retrieves the actual message being conveyed.
     * 
     * @return String, the message.
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Sets this message's message string to the new message.
     * 
     * @param message The new message.
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Retrieves the message id number.
     * 
     * @return int, the id.
     */
    public int getMessageId() {
        return this.messageId;
    }

    /**
     * Retrieves the message timestamp.
     * 
     * @return String, the timestamp.
     */
    public String getTimestamp() {
        return this.timestamp;
    }

    /**
     * Retrieves the id of the channel this message is in.
     * 
     * @return int, the channel id. 
     */
    public int getChannelId() {
        return this.associatedChannelId;
    }
}
