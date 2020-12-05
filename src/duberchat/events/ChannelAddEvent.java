package duberchat.events;

import duberchat.chatutil.Channel;

public class ChannelAddEvent extends ChannelEvent {
    static final long serialVersionUID = 1L;

    public ChannelAddEvent(Object source, Channel channel) {
        super(source, channel);
    }
}
