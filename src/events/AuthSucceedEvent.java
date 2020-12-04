package events;

import java.util.HashMap;
import java.util.ArrayList;
import chatutil.Message;

public class AuthSucceedEvent extends AuthEvent {
  static final long serialVersionUID = 1L;

  protected int userId;
  protected HashMap<String, Integer> channelNameMap;
  protected HashMap<Integer, Boolean> adminList;
  protected ArrayList<Message> recentChannelMessages;

  public AuthSucceedEvent(Object source, int userId, HashMap<String, Integer> channelNameMap,
      HashMap<Integer, Boolean> adminList, ArrayList<Message> recentChannelMessages) {
    super(source);

    this.userId = userId;
    this.channelNameMap = channelNameMap;
    this.adminList = adminList;
    this.recentChannelMessages = recentChannelMessages;
  }

  public int getUserId() {
    return this.userId;
  }

  public HashMap<String, Integer> getChannelNameMap() {
    return this.channelNameMap;
  }

  public HashMap<Integer, Boolean> getAdminList() {
    return this.adminList;
  }

  public ArrayList<Message> getRecentChannelMessages() {
    return this.recentChannelMessages;
  }
}
