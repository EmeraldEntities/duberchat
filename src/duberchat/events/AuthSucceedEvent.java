package duberchat.events;

import duberchat.chatutil.Message;
import duberchat.chatutil.User;
import duberchat.chatutil.Channel;

import java.util.HashMap;
import java.util.ArrayList;

public class AuthSucceedEvent extends AuthEvent {
    static final long serialVersionUID = 1L;

    protected User user;
    protected HashMap<Integer, Channel> channels;
    protected ArrayList<Message> recentChannelMessages;

    public AuthSucceedEvent(Object source, User user, HashMap<Integer, Channel> channels) {
        super(source);

        this.user = user;
        this.channels = channels;
    }

    public User getUser() {
        return this.user;
    }

    public HashMap<Integer, Channel> getChannels() {
        return this.channels;
    }
}
