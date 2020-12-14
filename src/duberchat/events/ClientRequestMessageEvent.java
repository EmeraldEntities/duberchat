package duberchat.events;

import java.util.ArrayList;
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

    protected int startMsgId;
    protected int channelId;
    protected ArrayList<Message> newMessageBlock;

    /**
     * Constructs a new {@code ClientRequestMessageEvent}.
     * 
     * @param source     The source of this event.
     * @param startMsgId The message to begin loading from, non-inclusive.
     * @param channelId  The associated channel.
     */
    public ClientRequestMessageEvent(Object source, int startMsgId, int channelId) {
        super(source);

        this.startMsgId = startMsgId;
        this.channelId = channelId;
    }

    /**
     * Constructs a new {@code ClientRequestMessageEvent}.
     * 
     * @param source          The source of this event.
     * @param startMsgId      The message to begin loading from, non-inclusive.
     * @param channelId       The associated channel.
     * @param newMessageBlock The new block of messages as a result of loading.
     */
    public ClientRequestMessageEvent(Object source, int startMsgId, int channelId, ArrayList<Message> newMessageBlock) {
        super(source);

        this.startMsgId = startMsgId;
        this.channelId = channelId;
        this.newMessageBlock = newMessageBlock;
    }
    
    /**
     * Retrieves this event's starting message id.
     * 
     * @return this event's new start message id.
     */
    public int getStartMsgId() {
        return this.startMsgId;
    }

    /**
     * Retrieves this event's new channel id.
     * 
     * @return this event's new channel id.
     */
    public int getChannelId() {
        return this.channelId;
    }

    /**
     * Retrieves this event's associated message block with the new messages.
     * 
     * @return a {@code ArrayList} object with this event's new message block.
     */
    public ArrayList<Message> getNewMessageBlock() {
        return this.newMessageBlock;
    }
}
