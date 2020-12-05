package duberchat.events;

import duberchat.chatutil.Channel;
import java.util.EventObject;

/**
 * A {@code ChannelEvent} is a generic, abstract event that represents any
 * action performed involving a channel.
 * <p>
 * This includes events like adding a member to a channel, creating a channel,
 * etc. The client shoudl always send a form of {@code ChannelEvent} that the
 * server receives, parses, and propagates to any other client in the channel.
 * 
 * <p>
 * Since <b>2020-12-04</b>.
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Joseph Wang
 * @see duberchat.chatutil.Channel
 */
public abstract class ChannelEvent extends EventObject {
    static final long serialVersionUID = 1L;
    protected Channel associatedChannel;

    /**
     * Constructs a new {@code ChannelEvent}.
     * 
     * @param source  The source of this event.
     * @param channel The associated channel with this event.
     */
    public ChannelEvent(Object source, Channel channel) {
        super(source);

        this.associatedChannel = channel;
    }

    /**
     * Retrieves this event's associated channel.
     * <p>
     * This is the channel that the event occured in, or {@code null} if the client
     * is sending this event and it does not know the specific channel (eg.
     * {@link duberchat.events.ChannelCreateEvent ChannelCreateEvent}).
     * 
     * @return a {@code Channel} object with the associated channel.
     */
    public Channel getChannel() {
        return this.associatedChannel;
    }
}
