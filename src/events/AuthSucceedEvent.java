package events;

import chatutil.Message;
import chatutil.User;

import java.util.HashMap;
import java.util.ArrayList;

public class AuthSucceedEvent extends AuthEvent {
    static final long serialVersionUID = 1L;

    protected User user;
    protected HashMap<String, Integer> channelNameMap;
    protected HashMap<Integer, Boolean> adminList;
    protected ArrayList<Message> recentChannelMessages;

    public AuthSucceedEvent(Object source, User user, HashMap<String, Integer> channelNameMap,
            HashMap<Integer, Boolean> adminList, ArrayList<Message> recentChannelMessages) {
        super(source);

        this.user = user;
        this.channelNameMap = channelNameMap;
        this.adminList = adminList;
        this.recentChannelMessages = recentChannelMessages;
    }

    public User getUser() {
        return this.user;
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
