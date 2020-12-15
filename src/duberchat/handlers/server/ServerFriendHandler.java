package duberchat.handlers.server;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Iterator;

import duberchat.chatutil.Channel;
import duberchat.chatutil.User;
import duberchat.events.FileWriteEvent;
import duberchat.events.FriendAddEvent;
import duberchat.events.FriendEvent;
import duberchat.events.FriendRemoveEvent;
import duberchat.events.RequestFailedEvent;
import duberchat.events.SerializableEvent;
import duberchat.handlers.Handleable;
import duberchat.server.ChatServer;

/**
 * the {@code ServerFriendHandler} class provides the server-side
 * implementation for handling any {@code FriendEvent}.
 * <p>
 * <p>
 * Created <b>2020-12-12</b>
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Paula Yuan
 * @see duberchat.events.ChannelFriendEvent
 * @see duberchat.events.ChannelAddFriendEvent
 * @see duberchat.events.ChannelDeleteFriendEvent
 */
public class ServerFriendHandler implements Handleable {
  /** The associated server this handler is attached to. */
  private ChatServer server;

  /**
   * Constructs a new {@code ServerFriendHandler}.
   * 
   * @param server the server that this handler is attached to.
   */
  public ServerFriendHandler(ChatServer server) {
    this.server = server;
  }

  /**
   * {@inheritDoc}
   * <p>
   * Ensures that the server has properly updated users, updates files, and sends
   * the event to all relevant users.
   * 
   * @param newEvent {@inheritDoc}
   */
  public void handleEvent(SerializableEvent newEvent) {
    FriendEvent event = (FriendEvent) newEvent;
    String friendUsername = event.getFriendUsername();
    String userUsername = (String) event.getSource();
    User user = server.getAllUsers().get(userUsername);
    HashSet<String> userFriends = user.getFriends();
    ObjectOutputStream userOutput = server.getCurUsers().get(user).getOutputStream();
    boolean adding; // adding = true, removing = false

    User friend = server.getAllUsers().get(friendUsername);

    // if the friend doesn't exist or the friend is the user themeselves, return a
    // request failed event to the user who sent the event
    if (friend == null || friend.equals(user)) {
      try {
        userOutput.writeObject(new RequestFailedEvent(userUsername));
        userOutput.flush();
        userOutput.reset();
        server.getServerFrame().getTextArea()
            .append(userUsername + " tried to add an invalid friend. Request failed event sent.\n");
        return;
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    // update the friends sets depending on whether a friend is being added or removed
    if (userFriends.contains(friendUsername)) {
      adding = false;
      userFriends.remove(friendUsername);
      friend.getFriends().remove(userUsername);
    } else {
      adding = true;
      userFriends.add(friendUsername);
      friend.getFriends().add(userUsername);
    }

    // update both parties' user files
    // Also, update the file of every channel these users are in because the user information has changed. :/
    String userFilePath = "data/users/" + userUsername; 
    server.getFileWriteQueue().add(new FileWriteEvent(user, userFilePath));
    String friendFilePath = "data/users/" + friendUsername; 
    server.getFileWriteQueue().add(new FileWriteEvent(friend, friendFilePath));
    HashSet<Integer> unionChannels = new HashSet<Integer>(user.getChannels());
    unionChannels.addAll(friend.getChannels());
    Iterator<Integer> channelsItr = unionChannels.iterator();
    while (channelsItr.hasNext()) {
      int channelId = channelsItr.next();
      Channel channel = server.getChannels().get(channelId);
      String channelFilePath = "data/channels/" + channelId; 
      server.getFileWriteQueue().add(new FileWriteEvent(channel, channelFilePath));
    }

    // send the appropriate event back to the pair of friends, if they're both online
    try {
      if (server.getCurUsers().containsKey(friend)) {
        ObjectOutputStream friendOutput = server.getCurUsers().get(friend).getOutputStream();
        if (adding) {
          friendOutput.writeObject(new FriendAddEvent(userUsername, userUsername, user));
        } else {
          friendOutput.writeObject(new FriendRemoveEvent(userUsername, userUsername));
        }
        friendOutput.flush();
        friendOutput.reset();
      }
      if (adding) {
        userOutput.writeObject(new FriendAddEvent(userUsername, friendUsername, friend));
      } else {
        userOutput.writeObject(new FriendRemoveEvent(userUsername, friendUsername));
      }
      userOutput.flush();
      userOutput.reset();
      server.getServerFrame().getTextArea()
          .append(userUsername + " and " + friendUsername + " became friends and events sent to users\n");
    } catch (IOException e) {
      e.printStackTrace();
    }

  }
  
}
