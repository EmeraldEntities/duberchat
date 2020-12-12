package duberchat.handlers.server;

import duberchat.chatutil.User;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Iterator;

import duberchat.chatutil.Channel;
import duberchat.events.ClientStatusUpdateEvent;
import duberchat.events.FileWriteEvent;
import duberchat.events.SerializableEvent;
import duberchat.handlers.Handleable;
import duberchat.server.ChatServer;

public class ServerStatusChangeHandler implements Handleable {
  private ChatServer server;

  public ServerStatusChangeHandler(ChatServer server) {
    this.server = server;
  }

  public void handleEvent(SerializableEvent newEvent) {
    ClientStatusUpdateEvent event = (ClientStatusUpdateEvent) newEvent;
    User user = (User) event.getSource();
    User serverUser = server.getAllUsers().get(user.getUsername());
    int newStatus = event.getStatus();
    user.setStatus(newStatus);
    serverUser.setStatus(newStatus);

    // Update the user file
    String userFilePath = "data/users/" + user.getUsername() + ".txt";
    server.getFileWriteQueue().add(new FileWriteEvent(serverUser, userFilePath));

    // Send a status update event to every other user in every channel this user is in
    // Also, update every channel's file because the user information has changed. :/
    for (int channelId : user.getChannels()) {
      Channel channel = server.getChannels().get(channelId);
      String channelFilePath = "data/channels/" + channelId + ".txt";
      server.getFileWriteQueue().add(new FileWriteEvent(channel, channelFilePath));
      Iterator<User> itr = channel.getUsers().values().iterator();
      while (itr.hasNext()) {
        User member = itr.next();
        if (member.equals(user) || !server.getCurUsers().containsKey(member)) {
          continue;
        }
        System.out.println(channel.getChannelName() + " " + member.getUsername());
        ObjectOutputStream output = server.getCurUsers().get(member).getOutputStream();
        try {
          output.writeObject(new ClientStatusUpdateEvent(member, newStatus));
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

    // close down the appropriate client thread if the user logs off
    if (newStatus == 0) {
      server.getCurUsers().get(user).setRunning(false);
      server.getCurUsers().remove(user);
    }
  }
}
