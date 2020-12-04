package chatutil;

import java.io.Serializable;

public class Message implements Serializable {
  static final long serialVersionUID = 1L;

  private String message;
  private int messageId;
  private Channel associatedChannel;

  public Message(String message, int messageId, Channel channel) {
    this.message = message;
    this.messageId = messageId;
    this.associatedChannel = channel;
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
