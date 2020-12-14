package duberchat.events;

/**
 * A {@code ChannelHierarchyChangeEvent} is a generic event representing either
 * a user promotion or demotion within a channel.
 * <p>
 * Neither the client nor server should send this event, but rather one of its
 * subclass events. However, handlers will operate based on this event, as
 * promotion and demotion are nigh-identical operations.
 * <p>
 * 
 * 
 * Since <b>2020-12-04</b>.
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Paula Yuan
 */
public class ChannelHierarchyChangeEvent extends ChannelEvent {
    static final long serialVersionUID = 1L;
    private String username;

    /**
     * Constructs a new {@code ChannelHierarchyChangeEvent}.
     * 
     * @param source    The source of this event.
     * @param channelId The associated channel id with this event.
     * @param username  The username of the user to promote/demote.
     */
    public ChannelHierarchyChangeEvent(Object source, int channelId, String username) {
        super(source, channelId);
        this.username = username;
    }

    /**
     * Retrieves the {@code String} username of the user to promote/demote.
     * 
     * @returns String, the username of the user to promote/demote.
     */
    public String getUsername() {
        return this.username;
    }
}