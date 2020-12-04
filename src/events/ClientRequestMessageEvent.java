package events;

public class ClientRequestMessageEvent extends ClientEvent {
  static final long serialVersionUID = 1L;

  protected int startMsgId;
  protected int channelId;

  public ClientRequestMessageEvent(Object source, int startMsgId, int channelId) {
    super(source);

    this.startMsgId = startMsgId;
    this.channelId = channelId;
  }

  public int getStartMsgId() {
    return this.startMsgId;
  }

  public int getChannelId() {
    return this.channelId;
  }
}
