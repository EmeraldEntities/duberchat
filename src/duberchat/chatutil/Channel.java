package duberchat.chatutil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;

public class Channel implements Serializable {
    static final long serialVersionUID = 1L;

    public static final int MESSAGE_CLUSTER_AMT = 30;
    public static final int LOCAL_SAVE_AMT = MESSAGE_CLUSTER_AMT;

    /** A count of all the message clusters this local channel has loaded. */
    private int messageClusters;

    /** A collection of messages with newest messages being at the top. */
    private ArrayList<Message> messages;
    /** A list of users in this channel. */
    private ArrayList<User> users;
    /** A list of users that have admin permissions in this channel. */
    private HashSet<User> adminUsers;
    /** The name of this channel. */
    private String channelName;
    /** The channel ID of this channel. Read only. */
    private int channelId;

    /**
     * Constructs a new Channel object.
     * 
     * @param channelName The name of this channel.
     * @param channelId   The permanent, unique id of this channel.
     * @param users       A starting list of users.
     * @param adminUsers  A starting list of admin users. All users in here should
     *                    be in the users list as well.
     */
    public Channel(String channelName, int channelId, ArrayList<User> users, HashSet<User> adminUsers) {
        this.messages = new ArrayList<>(LOCAL_SAVE_AMT);
        this.users = users;
        this.adminUsers = adminUsers;

        this.channelName = channelName;
        this.channelId = channelId;

        this.messageClusters = 0;
    }

    /**
     * Loads a message cluster and appends it to the end of this channel's messages,
     * if possible.
     * <p>
     * The mess ge cluster must be the same size or less as dictated in the class
     * constant {@code MESSAGE_CLUSTER_AMT}, or the load will fail. exists so that,
     * for example, channels with 15 messages left will still load.
     * 
     * @param messageCluster the cluster of messages to add to this channel's
     *                       messages.
     * @return true if the messages were added, false otherwise.
     */
    public boolean loadMessageCluster(ArrayList<Message> messageCluster) {
        if (messageCluster.size() <= MESSAGE_CLUSTER_AMT) {
            this.messageClusters++;
            this.messages.addAll(messageCluster);
            return true;
        }

        return false;
    }

    /**
     * Retrieves all this channel's messages.
     * 
     * @return an {@code ArrayList} with this channel's messages.
     */
    public ArrayList<Message> getMessages() {
        return this.messages;
    }

    /**
     * Add a message to this channel's list of messages.
     * 
     * Since each channel will only locally store a certain amount of messages, if
     * the number of messages exceed the {@code LOCAL_SAVE_AMOUNT} constant (plus
     * any additional message clusters that have been loaded), the most recent
     * message will be deleted.
     * 
     * @param message a {@code Message} object to add.
     */
    public void addMessage(Message message) {
        // no matter what end we adjust, we'll still have to adjust indexes
        // adding to the top allows us to use addAll
        if (this.messages.size() >= LOCAL_SAVE_AMT + (messageClusters * MESSAGE_CLUSTER_AMT)) {
            this.messages.remove(this.messages.size());
        }

        this.messages.add(0, message);
    }

    /**
     * Gets this channel's list of users.
     * 
     * @return an {@code ArrayList} of this channel's list of users.
     */
    public ArrayList<User> getUsers() {
        return this.users;
    }

    /**
     * Adds a user to this channel's list of users.
     * 
     * @param user the user to be removed.
     */
    public void addUser(User user) {
        this.users.add(user);
    }

    /**
     * Removes a user from this channel's list of users.
     * <p>
     * This method assumes that the user provided is a reference to a user already
     * present in this channel's list of user, or a user object that equals an
     * existing user.
     * 
     * @param user the user to be removed.
     */
    public void removeUser(User user) {
        this.users.remove(user);
    }

    /**
     * Retrieves this channel's admin users.
     * 
     * @return this channel's admin users.
     */
    public HashSet<User> getAdminUsers() {
        return this.adminUsers;
    }

    /**
     * Add an user to this channel's list of admin users.
     *
     * @param user the new admin user.
     */
    public void addAdminUser(User user) {
        this.adminUsers.add(user);
    }

    /**
     * Removes a user from this channel's list of users.
     * <p>
     * The user provided should be a reference to a user that is present in the
     * admin user list, or an object that equals an existing user.
     * 
     * @param user the admin user to be removed.
     */
    public void removeAdminUser(User user) {
        this.adminUsers.remove(user);
    }

    /**
     * Checks if a provided user is admin in this channel.
     * 
     * @param uThe user to check.
     * @return true if the user is admin, false otherwise.
     */
    public boolean checkIfAdmin(User user) {
        return this.adminUsers.contains(user);
    }

    /**
     * Retrieves this channel's name.
     * 
     * @return the name of this channel.
     */
    public String getChannelName() {
        return this.channelName;
    }

    /**
     * Sets this channel name to a new name.
     * 
     * @param channelName the new name for this channel.
     */
    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    /**
     * Gets this channel's id.
     * 
     * @return this channel's id.
     */
    public int getChannelId() {
        return this.channelId;
    }
}
