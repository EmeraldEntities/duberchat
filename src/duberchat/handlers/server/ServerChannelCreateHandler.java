package duberchat.handlers.server;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import duberchat.chatutil.Channel;
import duberchat.chatutil.User;
import duberchat.events.ChannelCreateEvent;
import duberchat.events.RequestFailedEvent;
import duberchat.events.SerializableEvent;
import duberchat.handlers.Handleable;
import duberchat.server.ChatServer;
import duberchat.client.ChatClient;

public class ServerChannelCreateHandler implements Handleable {
  ChatServer server;

  public ServerChannelCreateHandler(ChatServer server) {
    this.server = server;
  }

  public void handleEvent(SerializableEvent newEvent) {
    ChannelCreateEvent event = (ChannelCreateEvent) newEvent;
    ObjectOutputStream output = server.getCurUsers().get((User) event.getSource()).getOutputStream();
    HashSet<String> usersFound = new HashSet<>();
    ArrayList<User> channelUsers = new ArrayList<>();
    String channelName = event.getChannel().getChannelName();
    boolean foundAUser = false;
    Iterator<String> itr = event.getUsernames().iterator();
    while (itr.hasNext()) {
      String username = itr.next();
      User user = server.getAllUsers().get(username);
      if (user != null) {
        foundAUser = true;
        usersFound.add(username);
        channelUsers.add(user);
      }
    }
    if (!foundAUser) {
      try {
        output.writeObject(new RequestFailedEvent(event));
        output.flush();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    User creator = ((User) event.getSource());
    usersFound.add(creator.getUsername());
    channelUsers.add(creator);
    HashSet<User> admins = new HashSet<>();
    admins.add(creator);
    int id = server.getChannels().size();
    Channel newChannel = new Channel(channelName, id, channelUsers, admins);
    server.getChannels().put(id, newChannel);
    try {

      // Add the new channel information and the appropriate filepath to the file write queue.
      String[] msgArr = new String[channelUsers.size() + 7];
      msgArr[0] = "data/channels/" + id + ".txt";
      msgArr[1] = id + "\n";
      msgArr[2] = channelName + "\n";
      msgArr[3] = "1\n";
      msgArr[4] = creator.getUsername() + "\n";
      msgArr[5] = channelUsers.size() + "\n";
      for (int i = 0; i < channelUsers.size(); i++) {
        msgArr[6 + i] = channelUsers.get(i).getUsername() + "\n";
      }
      msgArr[channelUsers.size() + 6] = "0 \n";
      server.getFileWriteQueue().add(msgArr);

      // Output a corresponding event to the client who made the channel
      output.writeObject(new ChannelCreateEvent((User) event.getSource(), newChannel, usersFound));
      output.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
