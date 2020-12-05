package duberchat.events;

import duberchat.chatutil.Channel;

/**
 * A {@code ChannelCreateEvent} is an event that is created when a new channel
 * is created.
 * <p>
 * The server is the one creating new channels and then propagating a new event
 * with the proper {@code Channel} object. As a direct result (and solution), if
 * the client is calling this event, then the associated channel is
 * {@code null}.
 * 
 * <p>
 * Since <b>2020-12-04</b>.
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Joseph Wang
 */
public class ChannelCreateEvent extends ChannelEvent {
    static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@code ChannelCreateEvent}.
     * 
     * @param source  The source of this event.
     * @param channel The associated channel with this event.
     */
    public ChannelCreateEvent(Object source, Channel channel) {
        super(source, channel);
    }
}
