package duberchat.server;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.imageio.ImageIO;

import duberchat.events.*;
import duberchat.gui.frames.ServerFrame;
import duberchat.handlers.Handleable;
import duberchat.handlers.server.*;
import duberchat.chatutil.*;

/**
 * This is the ChatServer class, a server that manages Duber Chat.
 * <p>
 * The server constantly looks for and establishes connections with clients. It
 * also constantly accepts client events, putting them into an event queue, and
 * later processes them through the dedicated event handler.
 * <p>
 * 2020-12-03
 * 
 * @since 0.1
 * @version 0.1
 * @author Mr. Mangat, Paula Yuan
 */
public class ChatServer {
    ServerSocket serverSock;// server socket for connection
    static boolean running = true; // controls if the server is accepting clients
    private HashMap<Integer, Channel> channels; // channel id to all channels
    private int numChannelsCreated;
    private ConcurrentHashMap<User, ConnectionHandler> curUsers; // map of all the online users to connection handler
                                                                 // runnables
    private ConcurrentHashMap<String, User> allUsers; // map of all the usernames to their users
    private LinkedBlockingQueue<SerializableEvent> eventQueue;
    private LinkedBlockingQueue<FileWriteEvent> fileWriteQueue;
    private LinkedBlockingQueue<FileWriteEvent> imageWriteQueue;
    private HashMap<Class<? extends SerializableEvent>, Handleable> eventHandlers;
    private HashMap<String, String> textConversions; // For text commands or emojis
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
        this.textConversions.put("/shrug", "¯\\_(ツ)_/¯");
        this.textConversions.put("/tableflip", "(╯°□°）╯︵ ┻━┻");
        this.textConversions.put("/unflip", "┬─┬ ノ( ゜-゜ノ)");
        this.textConversions.put(":)", "🙂");
        this.textConversions.put(":D", "😄");
        this.textConversions.put(":P", "😛");
        this.textConversions.put(":(", "😦");
        this.textConversions.put(";)", "😉");
        this.textConversions.put(":O", "😮");
        this.textConversions.put(":'(", "😢");
        this.textConversions.put(">:(", "😠");
        this.textConversions.put(":|", "😐");
        this.textConversions.put("<3", "❤");

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

        this.serverFrame.getTextArea().append("Waiting for a client connection..\n");

        Socket client = null; // hold the client connection

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

        try {
            serverSock = new ServerSocket(5000); // assigns an port to the server
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
                               ((ClientStatusUpdateEvent) event).getStatus() == 0) {
                            eventHandlers.get(ClientStatusUpdateEvent.class).handleEvent(event);
                            continue;
                    }
                    eventQueue.add(event);
                } catch (IOException e) {
                    ChatServer.this.serverFrame.getTextArea().append("Failed to receive msg from the client\n");
                    user.setStatus(0);
                    eventHandlers.get(ClientStatusUpdateEvent.class)
                            .handleEvent(new ClientStatusUpdateEvent(user.getUsername(), 0));
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

                user.setStatus(1);
                fileWriteQueue.add(new FileWriteEvent(user, "data/users/" + username));
                curUsers.put(user, this);

                // update channel files and all the users in those channels regarding this user's login
                HashMap<Integer, Channel> userChannels = new HashMap<>();
                Iterator<Integer> itr = user.getChannels().iterator();
                HashSet<User> notifiedAlready = new HashSet<>();
                notifiedAlready.add(user);
                while (itr.hasNext()) {
                    int id = itr.next();
                    Channel curChannel = channels.get(id);
                    Iterator<User> iterator = curChannel.getUsers().values().iterator();
                    while (iterator.hasNext()) {
                        User member = iterator.next();
                        if (!curUsers.containsKey(member) || notifiedAlready.contains(member)) {
                            continue;
                        } 
                        ObjectOutputStream userOut = curUsers.get(member).getOutputStream();
                        userOut.writeObject(new ClientStatusUpdateEvent(user.getUsername(), 1));
                        userOut.flush();
                        userOut.reset();
                        notifiedAlready.add(member);
                    }
                    fileWriteQueue.add(new FileWriteEvent(curChannel, "data/channels/" + id));
                    ArrayList<Message> messages = curChannel.getMessages();
                    ArrayList<Message> messageBlock = new ArrayList<>();
                    for (int i = Math.max(messages.size() - 30, 0); i < messages.size(); i++) {
                        messageBlock.add(messages.get(i));
                    }
                    Channel correctedChannel = new Channel(curChannel);
                    correctedChannel.setMessages(messageBlock);
                    userChannels.put(id, correctedChannel);
                }

                // retrieve the friends list
                HashMap<String, User> friendsMap = new HashMap<>();
                Iterator<String> friendsItr = user.getFriends().iterator();
                while (friendsItr.hasNext()) {
                    String friendUsername = friendsItr.next();
                    User friend = ChatServer.this.allUsers.get(friendUsername);
                    // Theoretically, the friend should never be null, but better safe than sorry.
                    if (friend != null) {
                        friendsMap.put(friendUsername, friend);
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

        public ObjectOutputStream getOutputStream() {
            return this.output;
        }

        public void setRunning(boolean newState) {
            this.running = newState;
        }

    } // end of inner class
} // end of Class
