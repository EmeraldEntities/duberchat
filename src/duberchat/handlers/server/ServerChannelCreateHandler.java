package duberchat.handlers.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;

import duberchat.chatutil.*;
import duberchat.events.ChannelCreateEvent;
import duberchat.events.FileWriteEvent;
import duberchat.events.SerializableEvent;
import duberchat.handlers.Handleable;
import duberchat.server.ChatServer;

/**
 * A {@code ServerChannelCreateHandler} is a handler that processes channel creation.
 * <p>
 * The handler receives a {@code ChannelCreateEvent} from the server, which itself came from the 
 * client. The handler adds the channel to the file writing queue and propagates the event with an  
 * updated {@code Channel} object to all existant users who should be in the channel.
 * 
 * <p>
 * Since <b>2020-12-05</b>.
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Paula Yuan
 */
public class ServerChannelCreateHandler implements Handleable {
  private ChatServer server;

  /**
   * Constructs a new {@code ServerChannelCreateHandler}.
   * 
   * @param server The associated server with this handler.
   */
  public ServerChannelCreateHandler(ChatServer server) {
    this.server = server;
  }

  /**
   * Handles the {@code ChannelCreateEvent}.
   * 
   * @param newEvent The {@code ChanenelCreateEvent} to handle..
   */
  public void handleEvent(SerializableEvent newEvent) {
    ChannelCreateEvent event = (ChannelCreateEvent) newEvent;
    ObjectOutputStream output = server.getCurUsers().get((User) event.getSource()).getOutputStream();
    HashSet<String> usersFound = new HashSet<>();
    LinkedHashMap<String, User> channelUsers = new LinkedHashMap<>();
    String channelName = event.getChannel().getChannelName();
    User creator = ((User) event.getSource());
    String creatorName = creator.getUsername();
    User serverCreator = server.getAllUsers().get(creatorName);

    channelUsers.put(creatorName, serverCreator);
    usersFound.add(creatorName);
    HashSet<User> admins = new HashSet<>();
    admins.add(serverCreator);
    Iterator<String> itr = event.getUsernames().iterator();
    while (itr.hasNext()) {
      String username = itr.next();
      // The channel creator is always automatically added, prevent them from being added twice
      User user = server.getAllUsers().get(username);
      if (user != null && user != serverCreator) {
        usersFound.add(username);
        channelUsers.put(username, user);
      }
    }

    // If this channel is a dm, check if the dm already exists.
    // If this channel is an already existing dm, return that dm instead.
    // both parties in a dm are admins of the dm; otherwise, only the creator starts off as admin
    // TODO: this is kinda scufffed lmao
    if (channelUsers.size() == 2) {
      for (int channelId : creator.getChannels()) {
        Channel channel = server.getChannels().get(channelId);
        // skip non-dms
        if (channel.getUsers().size() != 2) continue;
        Iterator<User> iterator = channel.getUsers().values().iterator();
        User user1 = iterator.next();
        User user2 = iterator.next();
        if (channelUsers.containsValue(user1) && channelUsers.containsValue(user2)) {
          ArrayList<Message> messageBlock = new ArrayList<>();
          ArrayList<Message> fullMessages = channel.getMessages();
          for (int i = 0; i < 30; i++) {
            messageBlock.add(fullMessages.get(fullMessages.size() - i));
          }
          try {
            output.writeObject(new ChannelCreateEvent(creator, new Channel(channel, messageBlock), 
                                                      usersFound));
            output.flush();
          } catch (IOException e) {
            e.printStackTrace();
          }
          return;
        }
      }
      Iterator<User> iterator = channelUsers.values().iterator();
      while (iterator.hasNext()) {
        admins.add(iterator.next());
      }
    }

    int id = server.getNumChannelsCreated() + 1;
    server.setNumChannelsCreated(id);
    Channel newChannel = new Channel(channelName, id, channelUsers, admins, 0);
    server.getChannels().put(id, newChannel);

    try {
      // Make a new file and write the channel object to it.
      System.out.println(newChannel);
      server.getFileWriteQueue().add(new FileWriteEvent(newChannel, "data/channels/" + id + ".txt"));
      
      // update all the users (and their files) with the new channel
      // Output a corresponding event to the user clients in the channel
      Iterator<User> iterator = channelUsers.values().iterator();
      while (iterator.hasNext()) {
        User user = iterator.next();
        user.getChannels().add(id);
        String filePath = "data/users/" + user.getUsername() + ".txt";
        server.getFileWriteQueue().add(new FileWriteEvent(user, filePath));
        if (!server.getCurUsers().containsKey(user)) {
          continue;
        }
        output = server.getCurUsers().get(user).getOutputStream();
        output.writeObject(new ChannelCreateEvent(creator, newChannel, usersFound));
        output.flush();
      }

    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
