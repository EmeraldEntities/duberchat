package duberchat.server;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import duberchat.events.*;
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
    private HashMap<String, String> textConversions; // For text commands
    private HashMap<Integer, Channel> channels; // channel id to all channels
    private int numChannelsCreated;
    private HashMap<User, ConnectionHandler> curUsers; // map of all the online users to connection handler runnables
    private HashMap<String, User> allUsers; // map of all the usernames to their users
    private ConcurrentLinkedQueue<SerializableEvent> eventQueue;
    private ConcurrentLinkedQueue<String[]> fileAppendQueue;
    private ConcurrentLinkedQueue<HashMap<String, HashMap<Integer, String>>> fileRewriteQueue;
    private HashMap<Class<? extends SerializableEvent>, Handleable> eventHandlers;

    public ChatServer() {
        this.curUsers = new HashMap<>();
        this.channels = new HashMap<>();
        this.textConversions = new HashMap<>();
        this.allUsers = new HashMap<>();
        this.eventHandlers = new HashMap<>();
        this.eventQueue = new ConcurrentLinkedQueue<>();
        this.fileAppendQueue = new ConcurrentLinkedQueue<>();
        this.fileRewriteQueue = new ConcurrentLinkedQueue<>();
        eventHandlers.put(ChannelCreateEvent.class, new ServerChannelCreateHandler(this));
        eventHandlers.put(MessageSentEvent.class, new ServerMessageSentHandler(this));
        eventHandlers.put(ChannelAddMemberEvent.class, new ServerChannelAddMemberHandler(this));
        eventHandlers.put(ChannelRemoveMemberEvent.class, new ServerChannelRemoveMemberHandler(this));
    }

    /**
     * Go Starts the server
     */
    public void go() {

        // load up all users and channels
        try {
            for (File userFile : new File("data/users").listFiles()) {
                BufferedReader reader = new BufferedReader(new FileReader(userFile));
                String username = reader.readLine().trim();
                // skip over password line
                reader.readLine();
                String pfpPath = reader.readLine().trim();
                this.allUsers.put(username, new User(username, pfpPath));
                reader.close();
            }
            for (File channelFile : new File("data/channels").listFiles()) {
                BufferedReader reader = new BufferedReader(new FileReader(channelFile));
                int id = Integer.parseInt(reader.readLine().trim());
                String name = reader.readLine().trim();
                int numAdmins = Integer.parseInt(reader.readLine().trim());
                HashSet<User> admins = new HashSet<>();
                for (int i = 0; i < numAdmins; i++) {
                    admins.add(allUsers.get(reader.readLine().trim()));
                }
                int numUsers = Integer.parseInt(reader.readLine().trim());
                ArrayList<User> users = new ArrayList<>();
                for (int i = 0; i < numUsers; i++) {
                    users.add(allUsers.get(reader.readLine().trim()));
                }
                int numTotalMsgs = Integer.parseInt(reader.readLine().trim());
                Channel newChannel = new Channel(name, id, users, admins, numTotalMsgs);
                String curLine = reader.readLine();
                while (curLine != null) {
                    String[] messageInfo = curLine.trim().split(" ");
                    newChannel.addMessage(new Message(messageInfo[3], messageInfo[2], Integer.parseInt(messageInfo[0]),
                            new Date(Long.parseLong(messageInfo[1])), newChannel));
                    curLine = reader.readLine();
                }
                channels.put(id, newChannel);
                reader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Waiting for a client connection..");

        Socket client = null; // hold the client connection

        try {
            serverSock = new ServerSocket(6969); // assigns an port to the server

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
            eventsThread.start();

            // Start new thread to handle file appending.
            // File appending is separated from file find&replace because find&replace involves
            // reading through (possibly) the whole file and rewriting it all, which is costly.
            Thread fileAppendThread = new Thread(new Runnable() {
                public void run() {
                    while (true) {
                        String[] msgs = fileAppendQueue.poll();
                        if (msgs == null) continue;
                        File toWriteTo = new File(msgs[0]);
                        try {
                            toWriteTo.createNewFile();
                            FileWriter writer = new FileWriter(toWriteTo, true);
                            for (int i = 1; i < msgs.length; i++) {
                                writer.write(msgs[i]);
                            }
                            writer.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }
            });
            fileAppendThread.start();

            Thread fileRewriteThread = new Thread(new Runnable() {
                public void run() {
                    while (true) {
                        HashMap<String, HashMap<Integer, String>> toRewrite = fileRewriteQueue.poll();
                        if (toRewrite == null) continue;
                        for (Map.Entry<String, HashMap<Integer, String>> entry : 
                             toRewrite.entrySet()) {
                            File rewriteFile = new File(entry.getKey());
                            HashMap<Integer, String> linesToRewrite = entry.getValue();
                            try {
                                BufferedReader reader = new BufferedReader(new FileReader(rewriteFile));
                                FileWriter writer = new FileWriter(rewriteFile);
                                int lineNum = 1;
                                String curLine = reader.readLine();
                                while (curLine != null) {
                                    if (linesToRewrite.containsKey(lineNum)) {
                                        writer.write(linesToRewrite.get(lineNum));
                                    } else {
                                        writer.write(curLine + "\n");
                                    }
                                    curLine = reader.readLine();
                                    lineNum++;
                                }
                                reader.close();
                                writer.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
            fileRewriteThread.start();

            while (running) { // this loops to accept multiple clients
                client = serverSock.accept(); // wait for connection
                System.out.println("Client connected");
                Thread t = new Thread(new ConnectionHandler(client));
                t.start(); // start the new thread
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error accepting connection");
            // close all and quit
            try {
                client.close();
            } catch (Exception e1) {
                System.out.println("Failed to close socket");
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

    public ConcurrentLinkedQueue<String[]> getFileAppendQueue() {
        return this.fileAppendQueue;
    }

    public ConcurrentLinkedQueue<HashMap<String, HashMap<Integer, String>>> getFileRewriteQueue() {
        return this.fileRewriteQueue;
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
                    System.out.println("Received a message");
                    System.out.println(event);

                    // ClientLoginEvents are handled separately because there may be no user-thread
                    // mapping that can inform the handler of what client to output to.
                    if (event instanceof ClientLoginEvent) {
                        handleLogin((ClientLoginEvent) event);
                        continue;
                    }
                    eventQueue.add(event);
                } catch (IOException e) {
                    System.out.println("Failed to receive msg from the client");
                    e.printStackTrace();
                } catch (ClassNotFoundException e1) {
                    System.out.println("Class not found :(");
                    e1.printStackTrace();
                }
            }

            // close the socket
            try {
                output.close();
                input.close();
                client.close();
            } catch (Exception e) {
                System.out.println("Failed to close socket");
            }
        } // end of run()

        public void handleLogin(ClientLoginEvent event) {
            String username = event.getUsername();
            int hashedPassword = event.getHashedPassword();

            // Case 1: new user
            if (event.getIsNewUser()) {
                try {
                    // If the username is already taken, send auth failed event
                    if (ChatServer.this.allUsers.containsKey(username)) {
                        output.writeObject(new AuthFailedEvent(event));
                        output.flush();
                        return;
                    }

                    // Add new user file to file write queue.
                    String[] msgArr = {"data/users/" + username + ".txt", username + "\n",
                                       hashedPassword + "\n", "default.png\n", "0\n"};
                    ChatServer.this.fileAppendQueue.add(msgArr);

                    System.out.println("Made new user.");
                    user = new User(username);
                    // TODO: NOTE: NOT THREAD SAFE
                    ChatServer.this.allUsers.put(username, user);
                    ChatServer.this.curUsers.put(user, this);
                    output.writeObject(new AuthSucceedEvent(event, user, new HashMap<Integer, Channel>()));
                    output.flush();

                    System.out.println("SYSTEM: Sent auth event.");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return;
            }

            // Case 2: already registered user
            try {
                File userFile = new File("data/users/" + username + ".txt");
                BufferedReader reader = new BufferedReader(new FileReader(userFile));
                // skip over username, password, and pfp lines
                // assumption is made that file was titled correctly (aka file title = username)
                for (int i = 0; i < 3; i++) {
                    reader.readLine();
                }
                // user should never be null; if it's null, FileNotFoundException
                // would've been caught
                user = allUsers.get(username);
                curUsers.put(user, this);
                int numChannels = Integer.parseInt(reader.readLine().trim());
                HashMap<Integer, Channel> userChannels = new HashMap<>();
                for (int i = 0; i < numChannels; i++) {
                    int channelId = Integer.parseInt(reader.readLine().trim());
                    userChannels.put(channelId, channels.get(channelId));
                }
                output.writeObject(new AuthSucceedEvent(event, user, userChannels));
                output.flush();
                reader.close();
            } catch (FileNotFoundException e) {
                try {
                    output.writeObject(new AuthFailedEvent(event));
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        public ObjectOutputStream getOutputStream() {
            return this.output;
        }

    } // end of inner class
} // end of Class
