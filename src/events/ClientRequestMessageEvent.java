package events;

import chatutil.Channel;
import chatutil.Message;

public class ClientRequestMessageEvent extends ClientEvent {
    static final long serialVersionUID = 1L;

    protected Message startMsg;
    protected Channel channel;

    public ClientRequestMessageEvent(Object source, Message startMsg, Channel channel) {
        super(source);

        this.startMsg = startMsg;
        this.channel = channel;
    }

    public Message getStartMsg() {
        return this.startMsg;
    }

    public Channel getChannel() {
        return this.channel;
    }
}
