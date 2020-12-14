package duberchat.handlers.server;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Iterator;

import duberchat.chatutil.Channel;
import duberchat.chatutil.User;
import duberchat.events.ChannelHierarchyChangeEvent;
import duberchat.events.ChannelPromoteMemberEvent;
import duberchat.events.ChannelDemoteMemberEvent;
import duberchat.events.FileWriteEvent;
import duberchat.events.SerializableEvent;
import duberchat.handlers.Handleable;
import duberchat.server.ChatServer;

public class ServerHierarchyHandler implements Handleable {
  private ChatServer server;

  public ServerHierarchyHandler(ChatServer server) {
    this.server = server;
  }

  public void handleEvent(SerializableEvent newEvent) {
    ChannelHierarchyChangeEvent event = (ChannelHierarchyChangeEvent) newEvent;
    Channel channel = server.getChannels().get(event.getChannel().getChannelId());
    User source = server.getAllUsers().get(((User) event.getSource()).getUsername());
    User toChange = server.getAllUsers().get(event.getUsername());
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
      Iterator<User> itr = channel.getUsers().values().iterator();
      while (itr.hasNext()) {
        User user = itr.next();
        if (!server.getCurUsers().containsKey(user)) continue;
        ObjectOutputStream output = server.getCurUsers().get(user).getOutputStream();
        if (promoting) {
          output.writeObject(new ChannelPromoteMemberEvent(new User(source), new Channel(channel), 
                                                           event.getUsername()));
        } else {
          output.writeObject(new ChannelDemoteMemberEvent(new User(source), new Channel(channel), 
                                                          event.getUsername()));
        }
        output.flush();
      }
      server.getServerFrame().getTextArea().append(toChange.getUsername() + "'s rank in channel "
          + channel.getChannelId() + "was changed and events sent to users\n");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
