package duberchat.events;

import duberchat.chatutil.Channel;

public class ChannelRemoveEvent extends ChannelEvent {
    static final long serialVersionUID = 1L;

    public ChannelRemoveEvent(Object source, Channel channel) {
        super(source, channel);
    }
}
