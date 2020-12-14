package duberchat.events;

/**
 * A {@code ChannelPromoteMemberEvent} is an event that is created when a user is
 * promoted from normal user to admin.
 * <p>
 * The client should send this event when a user is promoted in a channel. The
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
 * @author Paula Yuan
 */
public class ChannelPromoteMemberEvent extends ChannelHierarchyChangeEvent {
    static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@code ChannelPromoteMemberEvent}.
     * 
     * @param source    The source of this event.
     * @param channelId The associated channel's id with this event.
     * @param username  The username of the user to promote.
     */
    public ChannelPromoteMemberEvent(Object source, int channelId, String username) {
        super(source, channelId, username);
    }
}