package chatutil;

import java.io.Serializable;

public class Message implements Serializable {
  static final long serialVersionUID = 1L;

  private String message;
  private int messageId;
  private int channelId;

  public Message(String message, int messageId, int channelId) {
    this.message = message;
    this.messageId = messageId;
    this.channelId = channelId;
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

  public void setMessageId(int messageId) {
    this.messageId = messageId;
  }

  public int getChannelId() {
    return this.channelId;
  }

  public void setChannelId(int channelId) {
    this.channelId = channelId;
  }
}
