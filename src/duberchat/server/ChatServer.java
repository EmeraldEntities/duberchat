package duberchat.server;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import duberchat.events.*;
import duberchat.gui.frames.ServerFrame;
import duberchat.handlers.Handleable;
import duberchat.handlers.server.*;
import duberchat.chatutil.*;

/**
 * This is the ChatServer class, representing the server that manages Duber
 * Chat.
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
    private HashMap<User, ConnectionHandler> curUsers; // map of all the online users to connection handler runnables
    private HashMap<String, User> allUsers; // map of all the usernames to their users
    private ConcurrentLinkedQueue<SerializableEvent> eventQueue;
    private ConcurrentLinkedQueue<FileWriteEvent> fileWriteQueue;
    private ConcurrentLinkedQueue<FileWriteEvent> imageWriteQueue;
    private HashMap<Class<? extends SerializableEvent>, Handleable> eventHandlers;
    private HashMap<String, String> textConversions; // For text commands or emojis
    private ServerFrame serverFrame;

    public ChatServer() {
        this.curUsers = new HashMap<>();
        this.channels = new HashMap<>();
        this.allUsers = new HashMap<>();
        this.eventQueue = new ConcurrentLinkedQueue<>();
        this.fileWriteQueue = new ConcurrentLinkedQueue<>();
        this.imageWriteQueue= new ConcurrentLinkedQueue<>();

        this.eventHandlers = new HashMap<>();
        this.eventHandlers.put(MessageSentEvent.class, new ServerMessageSentHandler(this));
        this.eventHandlers.put(MessageDeleteEvent.class, new ServerMessageDeleteHandler(this));
        this.eventHandlers.put(MessageEditEvent.class, new ServerMessageEditHandler(this));
        this.eventHandlers.put(ChannelCreateEvent.class, new ServerChannelCreateHandler(this));
        this.eventHandlers.put(ChannelAddMemberEvent.class, new ServerChannelAddMemberHandler(this));
        this.eventHandlers.put(ChannelRemoveMemberEvent.class, new ServerChannelRemoveMemberHandler(this));
        this.eventHandlers.put(ChannelDeleteEvent.class, new ServerChannelDeleteHandler(this));
        this.eventHandlers.put(ClientProfileUpdateEvent.class, new ServerProfileUpdateHandler(this));
        this.eventHandlers.put(ClientRequestMessageEvent.class, new ServerRequestMessageHandler(this));
        ServerFriendHandler friendHandler = new ServerFriendHandler(this);
        this.eventHandlers.put(FriendAddEvent.class, friendHandler);
        this.eventHandlers.put(FriendRemoveEvent.class, friendHandler);
        ServerHierarchyHandler hierarchyHandler = new ServerHierarchyHandler(this);
        this.eventHandlers.put(ChannelPromoteMemberEvent.class, hierarchyHandler);
        this.eventHandlers.put(ChannelDemoteMemberEvent.class, hierarchyHandler);

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
     * Go Starts the server
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
                    FileWriteEvent writeInfo = fileWriteQueue.poll();
                    if (writeInfo == null) continue;
                    try {
                        String filePath = writeInfo.getFilePath();
                        FileOutputStream fileOut = new FileOutputStream(filePath);
                        ObjectOutputStream out = new ObjectOutputStream(fileOut);
                        out.writeObject(writeInfo.getObjectToWrite());
                        out.flush();
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
                    FileWriteEvent writeInfo = imageWriteQueue.poll();
                    if (writeInfo == null) continue;
                    try {
                        String filePath = writeInfo.getFilePath();
                        FileOutputStream fileOut = new FileOutputStream(filePath);
                        ObjectOutputStream out = new ObjectOutputStream(fileOut);

                        String extension = filePath.substring(filePath.lastIndexOf("."), filePath.length());
                        ImageIO.write((BufferedImage) (writeInfo.getObjectToWrite()), extension, out);
                        ChatServer.this.serverFrame.getTextArea().append("Wrote to image file: " + filePath +"\n");
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
                    SerializableEvent event = eventQueue.poll();
                    if (event == null) continue;
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

    public HashMap<Integer, Channel> getChannels() {
        return this.channels;
    }

    public int getNumChannelsCreated() {
        return this.numChannelsCreated;
    }

    public void setNumChannelsCreated(int newNum) {
        this.numChannelsCreated = newNum;
    }

    public HashMap<String, User> getAllUsers() {
        return this.allUsers;
    }

    public HashMap<User, ConnectionHandler> getCurUsers() {
        return this.curUsers;
    }

    public ConcurrentLinkedQueue<FileWriteEvent> getFileWriteQueue() {
        return this.fileWriteQueue;
    }

    public ConcurrentLinkedQueue<FileWriteEvent> getImageWriteQueue() {
        return this.imageWriteQueue;
    }

    public HashMap<String, String> getTextConversions() {
        return this.textConversions;
    }

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
                    } else if (event instanceof ClientProfileUpdateEvent && 
                               ((User) event.getSource()).getStatus() == 0) {
                            eventHandlers.get(ClientProfileUpdateEvent.class).handleEvent(event);
                            continue;
                    }
                    eventQueue.add(event);
                } catch (IOException e) {
                    ChatServer.this.serverFrame.getTextArea().append("Failed to receive msg from the client\n");
                    user.setStatus(0);
                    eventHandlers.get(ClientProfileUpdateEvent.class).handleEvent(new ClientProfileUpdateEvent(user));
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
                        output.writeObject(new AuthFailedEvent(event));
                        output.flush();
                        return;
                    }

                    ChatServer.this.serverFrame.getTextArea().append("New user created: " + username + "\n");
                    user = new User(username, password);

                    // make new user file
                    fileWriteQueue.add(new FileWriteEvent(user, "data/users/" + username));

                    // TODO: NOTE: NOT THREAD SAFE
                    ChatServer.this.allUsers.put(username, user);
                    ChatServer.this.curUsers.put(user, this);
                    ChatServer.this.serverFrame.getTextArea().append(username + "'s authentication succeded\n");
                    output.writeObject(new AuthSucceedEvent(event, user, 
                                                            new HashMap<Integer, Channel>(), 
                                                            new HashMap<String, User>()));
                    output.flush();

                    ChatServer.this.serverFrame.getTextArea().append("Sent " + username + "'s authentication event\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }

            // Case 2: already registered user
            try {
                // If user doesn't exist or password is wrong give back an auth failed event to the client.
                User user = allUsers.get(username);
                if (user == null || password != user.getHashedPassword()) {
                    output.writeObject(new AuthFailedEvent(event));
                    output.flush();
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
                        if (user.equals(member)) {
                            member.setStatus(1);
                        }
                        ObjectOutputStream userOut = curUsers.get(member).getOutputStream();
                        userOut.writeObject(new ClientProfileUpdateEvent(user));
                        userOut.flush();
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
                output.writeObject(new AuthSucceedEvent(event, user, userChannels, friendsMap));
                output.flush();
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
