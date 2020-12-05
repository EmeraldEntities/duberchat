package duberchat.events;

import duberchat.chatutil.Channel;
import java.util.EventObject;

public abstract class ChannelEvent extends EventObject {
    static final long serialVersionUID = 1L;
    protected Channel associatedChannel;

    public ChannelEvent(Object source, Channel channel) {
        super(source);

        this.associatedChannel = channel;
    }

    public Channel getChannel() {
        return this.associatedChannel;
    }
}
