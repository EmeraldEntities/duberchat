package events;

public class ChannelAddEvent extends ChannelEvent {
  static final long serialVersionUID = 1L;

  public ChannelAddEvent(Object source, int channelId) {
    super(source, channelId);
  }
}
