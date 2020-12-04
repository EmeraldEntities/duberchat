package chatutil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;

public class Channel implements Serializable {
  static final long serialVersionUID = 1L;

  private ArrayList<Message> messages;
  private ArrayList<User> users;
  private HashSet<User> adminUsers;
  private String channelName;
  private int channelId;

  public Channel(String channelName, int channelId, ArrayList<User> users, HashSet<User> adminUsers) {
    this.messages = new ArrayList<>(30);
    this.users = users;
    this.adminUsers = adminUsers;

    this.channelName = channelName;
    this.channelId = channelId;
  }

  public ArrayList<Message> getMessages() {
    return this.messages;
  }

  // TODO: make addMessage which adds one and removes one if needed

  public ArrayList<User> getUsers() {
    return this.users;
  }

  // TODO: make addUser and removeUser

  public HashSet<User> getAdminUsers() {
    return this.adminUsers;
  }

  // TODO: make addAdminUser and removeAdminUser

  public String getChannelName() {
    return this.channelName;
  }

  public void setChannelName(String channelName) {
    this.channelName = channelName;
  }

  public int getChannelId() {
    return this.channelId;
  }
}
