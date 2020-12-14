package duberchat.handlers.server;

import java.io.IOException;
import java.io.ObjectOutputStream;

import duberchat.chatutil.Channel;
import duberchat.chatutil.User;
import duberchat.events.ChannelHierarchyChangeEvent;
import duberchat.events.ChannelPromoteMemberEvent;
import duberchat.events.ChannelDemoteMemberEvent;
import duberchat.events.FileWriteEvent;
import duberchat.events.SerializableEvent;
import duberchat.handlers.Handleable;
import duberchat.server.ChatServer;

/**
 * the {@code ServerHierarchyHandler} class provides the server-side
 * implementation for handling any {@code ChannelHierarchyEvent}.
 * <p>
 * <p>
 * Created <b>2020-12-12</b>
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Paula Yuan
 * @see duberchat.events.ChannelHierarchyEvent
 * @see duberchat.events.ChannelPromoteMemberEvent
 * @see duberchat.events.ChannelDemoteMemberEvent
 */
public class ServerHierarchyHandler implements Handleable {
  /** The associated server this handler is attached to. */
  private ChatServer server;

  /**
   * Constructs a new {@code ServerHierarchyHandler}.
   * 
   * @param server the server that this handler is attached to.
   */
  public ServerHierarchyHandler(ChatServer server) {
    this.server = server;
  }

  /**
   * {@inheritDoc}
   * <p>
   * Ensures that the server has a properly channel, updates files, and sends the
   * event to all relevant users.
   * 
   * @param newEvent {@inheritDoc}
   */
  public void handleEvent(SerializableEvent newEvent) {
    ChannelHierarchyChangeEvent event = (ChannelHierarchyChangeEvent) newEvent;
    Channel channel = server.getChannels().get(event.getChannelId());
    User source = server.getAllUsers().get((String) event.getSource());
    User toChange = server.getAllUsers().get(event.getUsername());

    // promote/demote the user in the server-side version of the channel
    boolean promoting;
    if (channel.getAdminUsers().contains(toChange)) {
      promoting = false;
      channel.removeAdminUser(toChange);
    } else {
      promoting = true;
      channel.addAdminUser(toChange);
    }

    String filePath = "data/channels/" + channel.getChannelId();
    try {
      // update the channel file
      server.getFileWriteQueue().add(new FileWriteEvent(channel, filePath));

      // give the appropriate event to all online users in the channel
      String sourceUsername = source.getUsername();
      int channelId = channel.getChannelId();
      String toChangeUsername = toChange.getUsername();
      for (User user : channel.getUsers().values()) {
        if (!server.getCurUsers().containsKey(user)) continue;
        ObjectOutputStream output = server.getCurUsers().get(user).getOutputStream();
        if (promoting) {
          output.writeObject(new ChannelPromoteMemberEvent(sourceUsername, channelId, toChangeUsername));
        } else {
          output.writeObject(new ChannelDemoteMemberEvent(sourceUsername, channelId, toChangeUsername));
        }
        output.flush();
        output.reset();
      }
      server.getServerFrame().getTextArea().append(toChange.getUsername() + "'s rank in channel "
          + channel.getChannelId() + "was changed and events sent to users\n");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
