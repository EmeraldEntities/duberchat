package events;

import java.util.EventObject;

public abstract class ChannelEvent extends EventObject {
  static final long serialVersionUID = 1L;
  protected int channelId;

  public ChannelEvent(Object source, int channelId) {
    super(source);

    this.channelId = channelId;
  }

  public int getChannelId() {
    return this.channelId;
  }
}
