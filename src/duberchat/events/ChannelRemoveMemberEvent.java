package duberchat.events;

import duberchat.chatutil.Channel;

/**
 * A {@code ChannelRemoveMemberEvent} is an event that is created when a user
 * should be removed from a channel.
 * <p>
 * The client should send this event when a user is removed from a channel. The
 * server will update and propagate the event (with an updated {@code Channel}
 * object) to the other clients, allowing for proper updating of channel
 * members.
 * <p>
 * Only admin users should be able to create this event.
 * 
 * <p>
 * Since <b>2020-12-04</b>.
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Joseph Wang
 */
public class ChannelRemoveMemberEvent extends ChannelEvent {
    static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@code ChannelRemoveMemberEvent}.
     * 
     * @param source  The source of this event.
     * @param channel The associated channel with this event.
     */
    public ChannelRemoveMemberEvent(Object source, Channel channel) {
        super(source, channel);
    }
}
