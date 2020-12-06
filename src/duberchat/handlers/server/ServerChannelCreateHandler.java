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
import duberchat.server.ChatServer.ConnectionHandler;
import duberchat.client.ChatClient;

public class ServerChannelCreateHandler implements Handleable {
  private HashMap<User, ConnectionHandler> onlineUsers;
  private HashMap<String, User> serverUsers;
  private HashMap<Integer, Channel> serverChannels;

  public ServerChannelCreateHandler(HashMap<User, ConnectionHandler> onlineUsers,
                                    HashMap<String, User> serverUsers, 
                                    HashMap<Integer, Channel> serverChannels) {
    this.onlineUsers = onlineUsers;
    this.serverUsers = serverUsers;
    this.serverChannels = serverChannels;
  }

  public void handleEvent(SerializableEvent newEvent) {
    ChannelCreateEvent event = (ChannelCreateEvent) newEvent;
    ObjectOutputStream output = onlineUsers.get((User) event.getSource()).getOutputStream();
    HashSet<String> usersFound = new HashSet<>();
    ArrayList<User> channelUsers = new ArrayList<>();
    String channelName = "";
    boolean foundAUser = false;
    Iterator<String> itr = event.getUsernames().iterator();
    while (itr.hasNext()) {
      String username = itr.next();
      User user = serverUsers.get(username);
      if (user != null) {
        foundAUser = true;
        usersFound.add(username);
        channelUsers.add(user);
        channelName += username + " ";
      }
    }
    channelName.trim();
    if (!foundAUser) {
      try {
        //TODO: fix null source
        output.writeObject(new RequestFailedEvent(null));
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
    int id = serverChannels.size();
    Channel newChannel = new Channel(channelName, id, channelUsers, admins);
    serverChannels.put(id, newChannel);
    File channelFile = new File("data/channels/" + id + ".txt");
    try {
      boolean created = channelFile.createNewFile();
      // If the id is already taken, something has gone extremely wrong.
      if (!created) {
        // TODO: should i throw an exception here or somethign?
        System.out.println("go check that map");
        return;
      }

      // Create the new channel file.
      FileWriter writer = new FileWriter(channelFile);
      writer.write(id + "\n");
      writer.write(channelName + "\n");
      writer.write(1 + "\n");
      writer.write(creator.getUsername() + "\n");
      writer.write(channelUsers.size() + "\n");
      for (User user : channelUsers) {
        writer.write(user.getUsername() + "\n");
      }
      //TODO: fix null source
      output.writeObject(new ChannelCreateEvent(null, newChannel, usersFound));
      output.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
