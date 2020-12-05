package duberchat.events;

import duberchat.chatutil.Channel;
import duberchat.chatutil.Message;

/**
 * A {@code ClientRequestMessageEvent} is an event that is created when a client
 * requests more messages from a specific channel.
 * <p>
 * Although this event is closely related with a
 * {@link duberchat.events.ChannelEvent ChannelEvent}, only one client should be
 * affected by this event (as only the sending client should be made to load
 * more messages from the channel). As a direct result, this event fits the
 * description imposed by a {@link duberchat.events.ClientEvent ClientEvent}, in
 * that this event only impacts one client.
 * <p>
 * Servers that serve this event should adhere as best as possible to the
 * cluster constant set in the {@link duberchat.chatutil.Channel Channel
 * object.} Returning a cluster that has less messages than the constant though
 * should be accepted and expected, as if the channel hits the beginning
 * messages, there is no guarantee that there will be 30 unique messages
 * remaining to return.
 * <p>
 * If this event fails, a {@link duberchat.events.RequestFailedEvent
 * RequestFailedEvent} should be returned to the client.
 * 
 * <p>
 * Since <b>2020-12-04</b>.
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Joseph Wang
 */
public class ClientRequestMessageEvent extends ClientEvent {
    static final long serialVersionUID = 1L;

    protected Message startMsg;
    protected Channel channel;

    /**
     * Constructs a new {@code ClientRequestMessageEvent}.
     * 
     * @param source   The source of this event.
     * @param startMsg The message to begin loading from, non-inclusive.
     * @param channel  The associated channel.
     */
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
