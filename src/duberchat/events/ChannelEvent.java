package duberchat.events;

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
public abstract class ChannelEvent extends SerializableEvent {
    static final long serialVersionUID = 1L;
    protected int associatedChannel;

    /**
     * Constructs a new {@code ChannelEvent}.
     * 
     * @param source    The source of this event.
     * @param channelId The associated channel id with this event.
     */
    public ChannelEvent(Object source, int channelId) {
        super(source);

        this.associatedChannel = channelId;
    }

    /**
     * Retrieves this event's associated channel id.
     * <p>
     * This is the id of the channel that the event occured in, or {@code -1} if the client
     * is sending this event and it does not know the specific channel (eg.
     * {@link duberchat.events.ChannelCreateEvent ChannelCreateEvent}).
     * 
     * @return a {@code Channel} object with the associated channel.
     */
    public int getChannelId() {
        return this.associatedChannel;
    }
}
