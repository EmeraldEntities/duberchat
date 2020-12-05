package duberchat.events;

import duberchat.chatutil.Channel;

/**
 * A {@code ChannelDeleteEvent} is an event that is created when a client
 * deletes a channel.
 * <p>
 * This event signifies to all channel users that this channel should no longer
 * exist. This event should only be called by an admin user of that channel.
 * <p>
 * Since <b>2020-12-04</b>.
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Joseph Wang
 */
public class ChannelDeleteEvent extends ChannelEvent {
    static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@code ChannelDeleteEvent}.
     * 
     * @param source  The source of this event.
     * @param channel The associated channel with this event.
     */
    public ChannelDeleteEvent(Object source, Channel channel) {
        super(source, channel);
    }
}
