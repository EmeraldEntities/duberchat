package duberchat.server;

import java.awt.image.BufferedImage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import java.net.ServerSocket;
import java.net.Socket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import javax.imageio.ImageIO;

import duberchat.chatutil.Channel;
import duberchat.chatutil.Message;
import duberchat.chatutil.User;

import duberchat.events.AuthFailedEvent;
import duberchat.events.AuthSucceedEvent;
import duberchat.events.ChannelAddMemberEvent;
import duberchat.events.ChannelCreateEvent;
import duberchat.events.ChannelDeleteEvent;
import duberchat.events.ChannelDemoteMemberEvent;
import duberchat.events.ChannelPromoteMemberEvent;
import duberchat.events.ChannelRemoveMemberEvent;
import duberchat.events.ClientLoginEvent;
import duberchat.events.ClientPasswordUpdateEvent;
import duberchat.events.ClientPfpUpdateEvent;
import duberchat.events.ClientRequestMessageEvent;
import duberchat.events.ClientStatusUpdateEvent;
import duberchat.events.FileWriteEvent;
import duberchat.events.FriendAddEvent;
import duberchat.events.FriendRemoveEvent;
import duberchat.events.MessageDeleteEvent;
import duberchat.events.MessageEditEvent;
import duberchat.events.MessageSentEvent;
import duberchat.events.SerializableEvent;

import duberchat.gui.frames.ServerFrame;

import duberchat.handlers.Handleable;
import duberchat.handlers.server.ServerChannelAddMemberHandler;
import duberchat.handlers.server.ServerChannelCreateHandler;
import duberchat.handlers.server.ServerChannelDeleteHandler;
import duberchat.handlers.server.ServerChannelRemoveMemberHandler;
import duberchat.handlers.server.ServerFriendHandler;
import duberchat.handlers.server.ServerHierarchyHandler;
import duberchat.handlers.server.ServerMessageDeleteHandler;
import duberchat.handlers.server.ServerMessageEditHandler;
import duberchat.handlers.server.ServerMessageSentHandler;
import duberchat.handlers.server.ServerProfileUpdateHandler;
import duberchat.handlers.server.ServerRequestMessageHandler;

/**
 * This is the ChatServer class, a server that manages Duber Chat.
 * <p>
 * The server constantly looks for and establishes connections with clients. It
 * also constantly accepts client events, putting them into an event queue, and
 * later processes them through the dedicated event handler.
 * <p>
 * 2020-12-03
 * 
 * @since 1.00
 * @version 1.00
 * @author Mr. Mangat, Paula Yuan
 */
public class ChatServer {
    /** The server socket for connction. */
    ServerSocket serverSock;
    /** Controls whether the server is accepting clients. */
    static boolean running = true; 
    /** Maps channel ids, which identify channels, to the actual channls. */
    private HashMap<Integer, Channel> channels; 
    /** Represents the total number of channels created, used for channel id. */
    private int numChannelsCreated;
    /** Keeps track of currently connected users. */
    private ConcurrentHashMap<User, ConnectionHandler> curUsers; 
    /** Keeps track of all users stored in the system. */
    private ConcurrentHashMap<String, User> allUsers; 

    /** Queue to organize events that need to be processed. */
    private LinkedBlockingQueue<SerializableEvent> eventQueue;
    /** Queue to organize file writing events. */
    private LinkedBlockingQueue<FileWriteEvent> fileWriteQueue;
    /** Queue to organize file writing regarding images. */
    private LinkedBlockingQueue<FileWriteEvent> imageWriteQueue;
    /** Keeps track of event handlers and the events they handle. */
    private HashMap<Class<? extends SerializableEvent>, Handleable> eventHandlers;

    /** Keeps track of text commands and emojis and what commands map to what. */
    private HashMap<String, String> textConversions; 
    /** The GUI associated with the server. */
    private ServerFrame serverFrame;

    public ChatServer() {
        this.curUsers = new ConcurrentHashMap<>();
        this.channels = new HashMap<>();
        this.allUsers = new ConcurrentHashMap<>();
        this.eventQueue = new LinkedBlockingQueue<>();
        this.fileWriteQueue = new LinkedBlockingQueue<>();
        this.imageWriteQueue = new LinkedBlockingQueue<>();

        // set up event handlers
        this.eventHandlers = new HashMap<>();
        this.eventHandlers.put(MessageSentEvent.class, new ServerMessageSentHandler(this));
        this.eventHandlers.put(MessageDeleteEvent.class, new ServerMessageDeleteHandler(this));
        this.eventHandlers.put(MessageEditEvent.class, new ServerMessageEditHandler(this));
        this.eventHandlers.put(ChannelCreateEvent.class, new ServerChannelCreateHandler(this));
        this.eventHandlers.put(ChannelAddMemberEvent.class, new ServerChannelAddMemberHandler(this));
        this.eventHandlers.put(ChannelRemoveMemberEvent.class, new ServerChannelRemoveMemberHandler(this));
        this.eventHandlers.put(ChannelDeleteEvent.class, new ServerChannelDeleteHandler(this));
        this.eventHandlers.put(ClientRequestMessageEvent.class, new ServerRequestMessageHandler(this));
        ServerProfileUpdateHandler profileHandler = new ServerProfileUpdateHandler(this);
        this.eventHandlers.put(ClientStatusUpdateEvent.class, profileHandler);
        this.eventHandlers.put(ClientPfpUpdateEvent.class, profileHandler);
        this.eventHandlers.put(ClientPasswordUpdateEvent.class, profileHandler);
        ServerFriendHandler friendHandler = new ServerFriendHandler(this);
        this.eventHandlers.put(FriendAddEvent.class, friendHandler);
        this.eventHandlers.put(FriendRemoveEvent.class, friendHandler);
        ServerHierarchyHandler hierarchyHandler = new ServerHierarchyHandler(this);
        this.eventHandlers.put(ChannelPromoteMemberEvent.class, hierarchyHandler);
        this.eventHandlers.put(ChannelDemoteMemberEvent.class, hierarchyHandler);

        // set up text conversions / emojis
        this.textConversions = new HashMap<>();
        this.textConversions.put(":)", "\uD83D\uDE42");
        this.textConversions.put(":D", "\uD83D\uDE04");
        this.textConversions.put(":P", "\uD83D\uDE1B");
        this.textConversions.put(":(", "\uD83D\uDE26");
        this.textConversions.put(";)", "\uD83D\uDE09");
        this.textConversions.put(":O", "\uD83D\uDE2E");
        this.textConversions.put(":'(", "\uD83D\uDE22");
        this.textConversions.put(">:(", "\uD83D\uDE20");
        this.textConversions.put(":|", "\uD83D\uDE10");
        this.textConversions.put("<3", "\u2764");

        this.serverFrame = new ServerFrame();
    }

    /**
     * Starts the server. Pre-loads all users and channels, then starts up threads
     * for handling events, file/image writing, and accepting clients.
     */
    public void go() {

        // load up all users and channels
        try {
            for (File userFile : new File("data/users").listFiles()) {
                FileInputStream fileIn = new FileInputStream(userFile);
                ObjectInputStream in = new ObjectInputStream(fileIn);
                User user = (User) in.readObject();
                this.allUsers.put(user.getUsername(), user);
                in.close();
            }

            for (File channelFile : new File("data/channels").listFiles()) {
                FileInputStream fileIn = new FileInputStream(channelFile);
                ObjectInputStream in = new ObjectInputStream(fileIn);
                Channel channel = (Channel) in.readObject();
                this.channels.put(channel.getChannelId(), channel);
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        }
        this.serverFrame.getTextArea().append("User and channel preloading completed.\n");

        // start thread for normal file writing
        Thread fileWriteThread = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    FileWriteEvent writeInfo = null;
                    try {
                        writeInfo = fileWriteQueue.take();
                    } catch (InterruptedException e1) {
                        continue; // keep reading from the queue
                    }
                    try {
                        String filePath = writeInfo.getFilePath();
                        FileOutputStream fileOut = new FileOutputStream(filePath);
                        ObjectOutputStream out = new ObjectOutputStream(fileOut);
                        out.writeObject(writeInfo.getObjectToWrite());
                        out.flush();
                        out.reset();
                        out.close();
                        ChatServer.this.serverFrame.getTextArea().append("Wrote to file: " + filePath + "\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        this.serverFrame.getTextArea().append("Started file writing thread.\n");
        fileWriteThread.start();

        // start thread for image file writing 
        Thread imageWriteThread = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    FileWriteEvent writeInfo = null;
                    try {
                        writeInfo = imageWriteQueue.take();
                    } catch (InterruptedException e1) {
                        continue; // keep reading from the queue
                    }
                    try {
                        String filePath = writeInfo.getFilePath();
                        FileOutputStream fileOut = new FileOutputStream(filePath);
                        ObjectOutputStream out = new ObjectOutputStream(fileOut);

                        String extension = filePath.substring(filePath.lastIndexOf("."), filePath.length());
                        ImageIO.write((BufferedImage) (writeInfo.getObjectToWrite()), extension, out);
                        ChatServer.this.serverFrame.getTextArea().append("Wrote to image file: " + filePath + "\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        this.serverFrame.getTextArea().append("Started image writing thread.\n");
        imageWriteThread.start();

        // start new thread to handle events
        Thread eventsThread = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    SerializableEvent event = null;
                    try {
                        event = eventQueue.take();
                    } catch (InterruptedException e) {
                        continue; // keep reading from the queue
                    }
                    eventHandlers.get(event.getClass()).handleEvent(event);
                }
            }
        });
        this.serverFrame.getTextArea().append("Started event handler thread." + "\n");
        eventsThread.start();

        // start accepting client connections
        this.serverFrame.getTextArea().append("Waiting for a client connection..\n");
        Socket client = null; // hold the client connection
        try {
            this.serverSock = new ServerSocket(5000); // assigns an port to the server
            while (running) { // this loops to accept multiple clients
                client = serverSock.accept(); // wait for connection
                this.serverFrame.getTextArea().append("Client connected\n");
                Thread t = new Thread(new ConnectionHandler(client));
                t.start(); // start the new thread
                this.serverFrame.getTextArea().append("Started new client connection thread.\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
            this.serverFrame.getTextArea().append("Error accepting connection\n");
            // close all and quit
            try {
                client.close();
            } catch (Exception e1) {
                this.serverFrame.getTextArea().append("Failed to close socket\n");
            }
            System.exit(-1);
        }
    }

    /**
     * Retrieves this server's map of channel ids to channels.
     * 
     * @return a {@code HashMap} mapping the channel ids to the server channels.
     */
    public HashMap<Integer, Channel> getChannels() {
        return this.channels;
    }

    /**
     * Retrieves the total number of channels that have been created.
     * 
     * @return an integer, the total number of channels that have been created.
     */
    public int getNumChannelsCreated() {
        return this.numChannelsCreated;
    }

    /**
     * Assigns the total number of channels that have been created.
     * 
     * @param newNum The new total number of channels that have been created.
     */
    public void setNumChannelsCreated(int newNum) {
        this.numChannelsCreated = newNum;
    }

    /**
     * Retrieves this user's profile picture.
     * 
     * @return BufferedImage, the profile picture.
     */
    public ConcurrentHashMap<String, User> getAllUsers() {
        return this.allUsers;
    }

    /**
     * Retrieves this user's profile picture.
     * 
     * @return BufferedImage, the profile picture.
     */
    public ConcurrentHashMap<User, ConnectionHandler> getCurUsers() {
        return this.curUsers;
    }

    /**
     * Retrieves this user's profile picture.
     * 
     * @return BufferedImage, the profile picture.
     */
    public LinkedBlockingQueue<FileWriteEvent> getFileWriteQueue() {
        return this.fileWriteQueue;
    }

    /**
     * Retrieves this user's profile picture.
     * 
     * @return BufferedImage, the profile picture.
     */
    public LinkedBlockingQueue<FileWriteEvent> getImageWriteQueue() {
        return this.imageWriteQueue;
    }

    /**
     * Retrieves this user's profile picture.
     * 
     * @return BufferedImage, the profile picture.
     */
    public HashMap<String, String> getTextConversions() {
        return this.textConversions;
    }

    /**
     * Retrieves this user's profile picture.
     * 
     * @return BufferedImage, the profile picture.
     */
    public ServerFrame getServerFrame() {
        return this.serverFrame;
    }

    // ***** Inner class - thread for client connection
    public class ConnectionHandler implements Runnable, Serializable {
        private static final long serialVersionUID = 1L;
        private transient ObjectOutputStream output; // assign printwriter to network stream
        private transient ObjectInputStream input; // Stream for network input
        private transient Socket client; // keeps track of the client socket
        private User user;
        private boolean running;

        /*
         * ConnectionHandler Constructor
         * 
         * @param the socket belonging to this client connection
         */
        ConnectionHandler(Socket s) {
            this.client = s; // constructor assigns client to this
            this.user = null;
            try { // assign all connections to client
                this.output = new ObjectOutputStream(client.getOutputStream());
                this.input = new ObjectInputStream(client.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            running = true;
        }

        /*
         * run executed on start of thread
         */
        public void run() {
            // Get a message from the client
            SerializableEvent event;

            // Send a message to the client

            // Get a message from the client
            while (running) { // loop until a message is received
                try {
                    event = (SerializableEvent) input.readObject(); // get a message from the client
                    ChatServer.this.serverFrame.getTextArea().append("Received a " + event.getClass() + " event\n");

                    // ClientLoginEvents are handled separately because there may be no user-thread
                    // mapping that can inform the handler of what client to output to.
                    // ClientStatusUpdateEvents are handled separately if they indicate that the 
                    // user is logging out, since after this event the handler should not keep 
                    // looking for events to handle.
                    if (event instanceof ClientLoginEvent) {
                        handleLogin((ClientLoginEvent) event);
                        continue;
                    } else if (event instanceof ClientStatusUpdateEvent && 
                               ((ClientStatusUpdateEvent) event).getStatus() == User.OFFLINE) {
                            eventHandlers.get(ClientStatusUpdateEvent.class).handleEvent(event);
                            continue;
                    }
                    eventQueue.add(event);
                } catch (IOException e) {
                    ChatServer.this.serverFrame.getTextArea().append("Failed to receive msg from the client\n");
                    user.setStatus(User.OFFLINE);
                    eventHandlers.get(ClientStatusUpdateEvent.class)
                            .handleEvent(new ClientStatusUpdateEvent(user.getUsername(), User.OFFLINE));
                } catch (ClassNotFoundException e1) {
                    ChatServer.this.serverFrame.getTextArea().append("Class not found :(\n");
                    e1.printStackTrace();
                }
            }

            // close the socket
            try {
                output.close();
                input.close();
                client.close();
            } catch (Exception e) {
                ChatServer.this.serverFrame.getTextArea().append("Failed to close the socket\n");
            }
        } // end of run()

        /**
         * Processs user login based on whether they are a new user or not, retrieves
         * all necessary information, ensures the server copy of the user is updated,
         * and sends back the appropriate events to all relevant users.
         * <p>
         * 
         * @param event The login event to handle.
         */
        public void handleLogin(ClientLoginEvent event) {
            String username = event.getUsername();
            long password = event.getHashedPassword();

            // Case 1: new user
            if (event.getIsNewUser()) {
                try {
                    // If the username is already taken, send auth failed event
                    if (ChatServer.this.allUsers.containsKey(username)) {
                        ChatServer.this.serverFrame.getTextArea().append(username + "'s authentication failed\n");
                        output.writeObject(new AuthFailedEvent(null));
                        output.flush();
                        output.reset();
                        return;
                    }

                    ChatServer.this.serverFrame.getTextArea().append("New user created: " + username + "\n");
                    user = new User(username, password);

                    // make new user file
                    fileWriteQueue.add(new FileWriteEvent(user, "data/users/" + username));

                    ChatServer.this.allUsers.put(username, user);
                    ChatServer.this.curUsers.put(user, this);
                    ChatServer.this.serverFrame.getTextArea().append(username + "'s authentication succeded\n");
                    output.writeObject(new AuthSucceedEvent(null, user, 
                                                            new HashMap<Integer, Channel>(), 
                                                            new HashMap<String, User>()));
                    output.flush();
                    output.reset();

                    ChatServer.this.serverFrame.getTextArea().append("Sent " + username + "'s authentication event\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }

            // Case 2: already registered user
            try {
                // If user doesn't exist or password is wrong give back an auth failed event to the client.
                user = allUsers.get(username);
                if (user == null || password != user.getHashedPassword()) {
                    output.writeObject(new AuthFailedEvent(event));
                    output.flush();
                    output.reset();
                    return;
                }

                user.setStatus(User.ONLINE);
                fileWriteQueue.add(new FileWriteEvent(user, "data/users/" + username));
                ChatServer.this.curUsers.put(user, this);

                // update channel files and all the users in those channels regarding this user's login
                HashMap<Integer, Channel> userChannels = new HashMap<>();
                Iterator<Integer> itr = user.getChannels().iterator();
                HashSet<User> notifiedAlready = new HashSet<>();
                notifiedAlready.add(user);
                while (itr.hasNext()) {
                    int id = itr.next();
                    Channel curChannel = channels.get(id);

                    for (User member : curChannel.getUsers().values()) {
                        if (!ChatServer.this.curUsers.containsKey(member) || notifiedAlready.contains(member)) {
                            continue;
                        } 
                        ObjectOutputStream userOut = ChatServer.this.curUsers.get(member).getOutputStream();
                        userOut.writeObject(new ClientStatusUpdateEvent(username, User.ONLINE));
                        userOut.flush();
                        userOut.reset();
                        notifiedAlready.add(member);
                    }

                    fileWriteQueue.add(new FileWriteEvent(curChannel, "data/channels/" + id));
                    ArrayList<Message> messages = curChannel.getMessages();
                    ArrayList<Message> messageBlock = new ArrayList<>();
                    for (int i = Math.max(messages.size() - Channel.MESSAGE_CLUSTER_AMT, 0); i < messages.size(); i++) {
                        messageBlock.add(messages.get(i));
                    }
                    Channel correctedChannel = new Channel(curChannel);

                    // Set the user as online in every channel
                    correctedChannel.setMessages(messageBlock);
                    userChannels.put(id, correctedChannel);
                }

                // retrieve the friends list and update all friends regarding this user's login.
                HashMap<String, User> friendsMap = new HashMap<>();
                Iterator<String> friendsItr = user.getFriends().iterator();
                while (friendsItr.hasNext()) {
                    String friendUsername = friendsItr.next();
                    User friend = ChatServer.this.allUsers.get(friendUsername);
                    // Theoretically, the friend should never be null, but better safe than sorry.
                    if (friend != null) {
                        friendsMap.put(friendUsername, friend);
                        if (ChatServer.this.curUsers.containsKey(friend)) {
                            ObjectOutputStream friendOut = ChatServer.this.curUsers.get(friend).getOutputStream();
                            friendOut.writeObject(new ClientStatusUpdateEvent(username, User.ONLINE));
                        }
                    }
                }
                ChatServer.this.serverFrame.getTextArea().append(username + "'s authentication succeded\n");
                output.writeObject(new AuthSucceedEvent(null, user, userChannels, friendsMap));
                output.flush();
                output.reset();
                ChatServer.this.serverFrame.getTextArea().append("Sent " + username + "'s authentication event\n");
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        /**
         * Retrieves the outputstream associated with this client.
         * 
         * @return ObjectOutputStream, the associated outputstream.
         */
        public ObjectOutputStream getOutputStream() {
            return this.output;
        }

        /**
         * Sets whether this client is running or not, and as such, whether the server
         * should continue listening for events or close down the thread.
         * 
         * @return BufferedImage, the profile picture.
         */
        public void setRunning(boolean newState) {
            this.running = newState;
        }

    } // end of inner class
} // end of Class
