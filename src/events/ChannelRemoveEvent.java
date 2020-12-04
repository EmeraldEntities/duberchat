package events;

public class ChannelRemoveEvent extends ChannelEvent {
  static final long serialVersionUID = 1L;

  public ChannelRemoveEvent(Object source, int channelId) {
    super(source, channelId);
  }
}
