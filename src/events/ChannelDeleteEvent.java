package events;

import chatutil.Channel;

public class ChannelDeleteEvent extends ChannelEvent {
    static final long serialVersionUID = 1L;

    public ChannelDeleteEvent(Object source, Channel channel) {
        super(source, channel);
    }
}
