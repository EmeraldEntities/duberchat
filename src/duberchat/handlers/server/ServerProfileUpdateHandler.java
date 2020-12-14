package duberchat.handlers.server;

import duberchat.chatutil.User;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Iterator;

import duberchat.chatutil.Channel;
import duberchat.events.ClientPasswordUpdateEvent;
import duberchat.events.ClientPfpUpdateEvent;
import duberchat.events.ClientStatusUpdateEvent;
import duberchat.events.FileWriteEvent;
import duberchat.events.SerializableEvent;
import duberchat.handlers.Handleable;
import duberchat.server.ChatServer;

/**
 * the {@code ServerProfileUpdateHandler} class provides the server-side
 * implementation for handling any {@code ClientProfileUpdatEvent}.
 * <p>
 * <p>
 * Created <b>2020-12-10</b>
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Paula Yuan
 * @see duberchat.events.ClientProfileUpdateEvent
 * @see duberchat.events.ClientPfpUpdateEvent
 * @see duberchat.events.ClientStatusUpdateEvent
 * @see duberchat.events.ClientPasswordUpdateEvent
 */
public class ServerProfileUpdateHandler implements Handleable {
  /** The associated server this handler is attached to. */
  private ChatServer server;

  /**
   * Constructs a new {@code ServerProfileUpdateHandler}.
   * 
   * @param server the server that this handler is attached to.
   */
  public ServerProfileUpdateHandler(ChatServer server) {
    this.server = server;
  }

  /**
   * {@inheritDoc}
   * <p>
   * Ensures that the server has a properly updated user (and thus channels),
   * updates files, and sends the event to all relevant users.
   * 
   * @param newEvent {@inheritDoc}
   */
  public void handleEvent(SerializableEvent newEvent) {
    String username = (String) newEvent.getSource();
    User user = server.getAllUsers().get(username);

    // updat the server-side user depending on which type of event it is
    if (newEvent instanceof ClientStatusUpdateEvent) {
      ClientStatusUpdateEvent statusEvent = (ClientStatusUpdateEvent) newEvent;
      if (user.getStatus() != statusEvent.getStatus()) {
        user.setStatus(statusEvent.getStatus());
      }
    } else if (newEvent instanceof ClientPfpUpdateEvent) {
      ClientPfpUpdateEvent pfpEvent = (ClientPfpUpdateEvent) newEvent;
      System.out.println("user pfp: " + user.getPfp() + " event pfp: " + pfpEvent.getNewPfp());
      if (!user.pfpEquals(pfpEvent.getNewPfp())) {
        user.setPfp(pfpEvent.getNewPfp());
        user.setPfpFormat(pfpEvent.getPfpFormat());
        String imagePath = "data/images/" + username + "." + pfpEvent.getPfpFormat();
        server.getImageWriteQueue().add(new FileWriteEvent(user.getPfp(), imagePath));
      }
    } else {
      ClientPasswordUpdateEvent passwordEvent = (ClientPasswordUpdateEvent) newEvent;
      if (user.getHashedPassword() != passwordEvent.getHashedPassword()) {
        user.setHashedPassword(passwordEvent.getHashedPassword());
      }
    }

    HashSet<String> alreadyNotified = new HashSet<>();
    // close down the appropriate client thread if the user logs off
    // otherwise, send back the appropriate event to the client 
    if (user.getStatus() == 0) {
      server.getCurUsers().get(user).setRunning(false);
      server.getCurUsers().remove(user);
    } else {
      alreadyNotified.add(user.getUsername());
      ObjectOutputStream output = server.getCurUsers().get(user).getOutputStream();
      try {
        if (newEvent instanceof ClientStatusUpdateEvent) {
          output.writeObject(new ClientStatusUpdateEvent(username, user.getStatus()));
        } else if (newEvent instanceof ClientPfpUpdateEvent) {
          output.writeObject(new ClientPfpUpdateEvent(username, user.getPfp(), user.getPfpFormat()));
        } else {
          output.writeObject(new ClientPasswordUpdateEvent(username, user.getHashedPassword()));
        }
        output.flush();
        output.reset();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    // Send a status update event to every other user in every channel this user is in
    // as well as all of this user's friends.
    // Also, update every channel's file because the user information has changed.
    Iterator<String> friendsItr = user.getFriends().iterator();
    while (friendsItr.hasNext()) {
      String friendUsername = friendsItr.next();
      User friend = server.getAllUsers().get(friendUsername);
      if (!server.getCurUsers().containsKey(friend) || alreadyNotified.contains(friendUsername)) {
        continue;
      }
      alreadyNotified.add(friendUsername);
      ObjectOutputStream output = server.getCurUsers().get(friend).getOutputStream();
      try {
        if (newEvent instanceof ClientStatusUpdateEvent) {
          output.writeObject(new ClientStatusUpdateEvent(username, user.getStatus()));
        } else if (newEvent instanceof ClientPfpUpdateEvent) {
          output.writeObject(new ClientPfpUpdateEvent(username, user.getPfp(), user.getPfpFormat()));
        } else {
          output.writeObject(new ClientPasswordUpdateEvent(username, user.getHashedPassword()));
        }
        output.flush();
        output.reset();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    Iterator<Integer> channelsItr = user.getChannels().iterator();
    while (channelsItr.hasNext()) {
      int channelId = channelsItr.next();
      Channel channel = server.getChannels().get(channelId);
      for (User member : channel.getUsers().values()) {
        if (!server.getCurUsers().containsKey(member) || alreadyNotified.contains(member.getUsername())) {
          continue;
        }
        alreadyNotified.add(member.getUsername());
        ObjectOutputStream output = server.getCurUsers().get(member).getOutputStream();
        try {
          if (newEvent instanceof ClientStatusUpdateEvent) {
            output.writeObject(new ClientStatusUpdateEvent(username, user.getStatus()));
          } else if (newEvent instanceof ClientPfpUpdateEvent) {
            output.writeObject(new ClientPfpUpdateEvent(username, user.getPfp(), user.getPfpFormat()));
          } else {
            output.writeObject(new ClientPasswordUpdateEvent(username, user.getHashedPassword()));
          }
          output.flush();
          output.reset();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      String channelFilePath = "data/channels/" + channelId;
      server.getFileWriteQueue().add(new FileWriteEvent(channel, channelFilePath));
    }
    server.getServerFrame().getTextArea().append(user.getUsername() + "'s profile updated and events sent to users.\n");

    // Update the user file
    String userFilePath = "data/users/" + user.getUsername();
    server.getFileWriteQueue().add(new FileWriteEvent(user, userFilePath));
  }
}
