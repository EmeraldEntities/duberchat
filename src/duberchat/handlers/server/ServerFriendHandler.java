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

public class ServerFriendHandler implements Handleable {
  private ChatServer server;

  public ServerFriendHandler(ChatServer server) {
    this.server = server;
  }

  public void handleEvent(SerializableEvent newEvent) {
    FriendEvent event = (FriendEvent) newEvent;
    String friendUsername = event.getFriendUsername();
    String userUsername = ((User) event.getSource()).getUsername();
    User serverUser = server.getAllUsers().get(userUsername);
    HashSet<String> serverUserFriends = serverUser.getFriends();
    ObjectOutputStream userOutput = server.getCurUsers().get(serverUser).getOutputStream();
    boolean adding; // adding = true, removing = false

    User friend = server.getAllUsers().get(friendUsername);

    // if the friend doesn't exist or the friend is the user themeselves, return a
    // request failed event to the user who sent the event
    if (friend == null || friend.equals(serverUser)) {
      try {
        userOutput.writeObject(new RequestFailedEvent(new User(serverUser)));
        userOutput.flush();
        server.getServerFrame().getTextArea()
            .append(userUsername + " tried to add an invalid friend. Request failed event sent.\n");
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    // update the friends sets depending on whether a friend is being added or removed
    if (serverUserFriends.contains(friendUsername)) {
      adding = false;
      serverUserFriends.remove(friendUsername);
      friend.getFriends().remove(userUsername);
    } else {
      adding = true;
      serverUserFriends.add(friendUsername);
      friend.getFriends().add(userUsername);
    }

    // update both parties' user files
    // Also, update the file of every channel these users are in because the user information has changed. :/
    String userFilePath = "data/users/" + userUsername; 
    server.getFileWriteQueue().add(new FileWriteEvent(serverUser, userFilePath));
    String friendFilePath = "data/users/" + friendUsername; 
    server.getFileWriteQueue().add(new FileWriteEvent(friend, friendFilePath));
    HashSet<Integer> unionChannels = new HashSet<Integer>(serverUser.getChannels());
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
          friendOutput.writeObject(new FriendAddEvent(new User(serverUser), userUsername));
        } else {
          friendOutput.writeObject(new FriendRemoveEvent(new User(serverUser), userUsername));
        }
        friendOutput.flush();
      }
      if (adding) {
        userOutput.writeObject(new FriendAddEvent(new User(friend), friendUsername));
      } else {
        userOutput.writeObject(new FriendRemoveEvent(new User(friend), friendUsername));
      }
      userOutput.flush();
      server.getServerFrame().getTextArea()
          .append(userUsername + " and " + friendUsername + " became friends and events sent to users\n");
    } catch (IOException e) {
      e.printStackTrace();
    }

  }
  
}
