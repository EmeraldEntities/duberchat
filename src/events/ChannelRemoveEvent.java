package events;

import chatutil.Channel;

public class ChannelRemoveEvent extends ChannelEvent {
    static final long serialVersionUID = 1L;

    public ChannelRemoveEvent(Object source, Channel channel) {
        super(source, channel);
    }
}
