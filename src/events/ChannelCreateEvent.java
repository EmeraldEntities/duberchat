package events;

import chatutil.Channel;

public class ChannelCreateEvent extends ChannelEvent {
  static final long serialVersionUID = 1L;

  public ChannelCreateEvent(Object source, Channel channel) {
    super(source, channel);
  }
}
