package duberchat.client;

import java.io.IOException;
import java.io.EOFException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.net.Socket;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.HashMap;

import duberchat.chatutil.User;
import duberchat.chatutil.Channel;
import duberchat.events.AuthSucceedEvent;
import duberchat.events.ChannelAddMemberEvent;
import duberchat.events.ChannelCreateEvent;
import duberchat.events.ChannelDeleteEvent;
import duberchat.events.ChannelDemoteMemberEvent;
import duberchat.events.ChannelPromoteMemberEvent;
import duberchat.events.ChannelRemoveMemberEvent;
import duberchat.events.ClientPasswordUpdateEvent;
import duberchat.events.ClientRequestMessageEvent;
import duberchat.events.ClientStatusUpdateEvent;
import duberchat.events.ClientPfpUpdateEvent;
import duberchat.events.FriendAddEvent;
import duberchat.events.FriendRemoveEvent;
import duberchat.events.MessageDeleteEvent;
import duberchat.events.MessageEditEvent;
import duberchat.events.MessageSentEvent;
import duberchat.events.RequestFailedEvent;
import duberchat.events.SerializableEvent;
import duberchat.handlers.Handleable;
import duberchat.handlers.client.ClientChannelAddMemberHandler;
import duberchat.handlers.client.ClientChannelCreateHandler;
import duberchat.handlers.client.ClientChannelDeleteHandler;
import duberchat.handlers.client.ClientChannelRemoveMemberHandler;
import duberchat.handlers.client.ClientFriendAdjustHandler;
import duberchat.handlers.client.ClientHierarchyHandler;
import duberchat.handlers.client.ClientMessageDeleteHandler;
import duberchat.handlers.client.ClientMessageEditHandler;
import duberchat.handlers.client.ClientMessageSentHandler;
import duberchat.handlers.client.ClientPasswordUpdateHandler;
import duberchat.handlers.client.ClientPfpUpdateHandler;
import duberchat.handlers.client.ClientRequestFailedHandler;
import duberchat.handlers.client.ClientRequestMessageHandler;
import duberchat.handlers.client.ClientStatusUpdateHandler;
import duberchat.gui.frames.MainFrame;
import duberchat.gui.frames.LoginFrame;

/**
 * THe {@code ChatClient} class runs, starts, and maintains all client-side
 * operations involving the application.
 * <p>
 * All events are sent and read from here, and delegate to their respective
 * handlers. All client information is kept here, and accessed from here to
 * maintain a consistant, synchronized, steady list of constantly changing
 * structures.
 * <p>
 * A few of the methods must be synchronized to ensure thread safety, as
 * multiple threads may call specific methods.
 * <p>
 * Created <b> 2020-12-03 </b>
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Joseph Wang
 */
public class ChatClient {
    /** The ip to connect to. */
    private String ip = "";
    /** The port to connect to. */
    private int port = -1;

    /** The socket for connection. */
    private Socket servSocket;
    /** This client's user. */
    private User user;
    /** All the channels this client has. */
    private HashMap<Integer, Channel> channels;
    /** This client's friends. */
    private HashMap<String, User> friends;
    /** The event handlers for this client, to easily handle events. */
    private HashMap<Class<? extends SerializableEvent>, Handleable> eventHandlers;

    /** This client's current channel */
    private Channel currentChannel;

    /** The input stream from the server socket. */
    private ObjectInputStream input;
    /** The output stream from the server socket. */
    private ObjectOutputStream output;

    /** Whether this client is running or not. */
    private boolean running = true;
    /** Whether this client is currently logging in or not. */
    private boolean currentlyLoggingIn = true;
    /** Whether this client has closed or not. */
    private boolean hasClosed = false;

    /** A blocking queue for outgoing events to be sent. */
    private LinkedBlockingQueue<SerializableEvent> outgoingEvents;
    /** The associated login frame for this client. */
    private LoginFrame loginWindow;
    /** The associated main menu frame for this client. */
    private MainFrame mainMenu;

    /**
     * Starts up this client, and begins login attempts.
     * <p>
     * Once login succeeds, this method will listen and respond to events as well.
     * This should be the only method that listens to events at this point. This
     * method is implemented to prevent conflicts with the login method input and
     * this input.
     */
    public void start() {
        // call a method that connects to the server
        this.outgoingEvents = new LinkedBlockingQueue<>();
        this.initializeHandlers();
        this.initializeOutgoingEventWorker();
        this.initializeConnectionInformation();
        this.attemptConnection();

        this.login();

        mainMenu = new MainFrame(this);
        mainMenu.setVisible(true);

        while (this.running) {
            // a blocking call
            try {
                SerializableEvent newEvent = (SerializableEvent) input.readObject();

                this.eventHandlers.get(newEvent.getClass()).handleEvent(newEvent);
            } catch (EOFException e) {
                System.out.println("SYSTEM: End of input stream.");
            } catch (IOException e) {
                System.out.println("SYSTEM: Failed to obtain an event from the server.");
                this.forceLogout(1);
            } catch (ClassNotFoundException e) {
                System.out.println("SYSTEM: Attempted to handle unknown event.");
            }
        }

        this.logout();
    }

    /**
     * Attempts to initialize connection information by reading said information
     * from the "data/ipconfig" file, if it exists.
     */
    private void initializeConnectionInformation() {
        try {
            File ipSettings = new File("data/ipconfig");

            if (!ipSettings.exists()) {
                this.ip = "127.0.0.1";
                this.port = 6969;
                return;
            }

            BufferedReader reader = new BufferedReader(new FileReader(ipSettings));
            this.ip = reader.readLine();
            this.port = Integer.parseInt(reader.readLine());
            reader.close();

            System.out.println("SYSTEM: IP settings loaded. " + this.ip + ":" + this.port);
        } catch (IOException e) {
            System.out.println("SYSTEM: Could not load IP settings.");
        }
    }

    /**
     * Initializes all the event handlers and places them inside the eventHandlers
     * map.
     */
    private void initializeHandlers() {
        this.eventHandlers = new HashMap<>();

        this.eventHandlers.put(ChannelAddMemberEvent.class, new ClientChannelAddMemberHandler(this));
        this.eventHandlers.put(ChannelRemoveMemberEvent.class, new ClientChannelRemoveMemberHandler(this));
        
        ClientFriendAdjustHandler friendHandler = new ClientFriendAdjustHandler(this);
        ClientHierarchyHandler hierarchyHandler = new ClientHierarchyHandler(this);
        this.eventHandlers.put(FriendAddEvent.class, friendHandler);
        this.eventHandlers.put(FriendRemoveEvent.class, friendHandler);
        this.eventHandlers.put(ChannelPromoteMemberEvent.class, hierarchyHandler);
        this.eventHandlers.put(ChannelDemoteMemberEvent.class, hierarchyHandler);

        this.eventHandlers.put(ClientPfpUpdateEvent.class, new ClientPfpUpdateHandler(this));
        this.eventHandlers.put(ClientStatusUpdateEvent.class, new ClientStatusUpdateHandler(this));
        this.eventHandlers.put(ClientPasswordUpdateEvent.class, new ClientPasswordUpdateHandler(this));
        this.eventHandlers.put(ClientRequestMessageEvent.class, new ClientRequestMessageHandler(this));
        this.eventHandlers.put(ChannelCreateEvent.class, new ClientChannelCreateHandler(this));
        this.eventHandlers.put(ChannelDeleteEvent.class, new ClientChannelDeleteHandler(this));
        this.eventHandlers.put(RequestFailedEvent.class, new ClientRequestFailedHandler(this));

        this.eventHandlers.put(MessageSentEvent.class, new ClientMessageSentHandler(this));
        this.eventHandlers.put(MessageEditEvent.class, new ClientMessageEditHandler(this));
        this.eventHandlers.put(MessageDeleteEvent.class, new ClientMessageDeleteHandler(this));
    }

    /**
     * Attempts to login the user.
     * <p>
     * Since login is such a radically different protocal and has a different action
     * performed, this method serves as the handler for both AuthFailed and
     * AuthSucceed events.
     * <p>
     * No input stream should be used as the same time as this login method, as this
     * method will attempt to read from the input socket.
     */
    private void login() {
        loginWindow = new LoginFrame(this);
        loginWindow.setVisible(true);

        SerializableEvent authEvent;
        while (this.currentlyLoggingIn) {
            try {
                if (this.servSocket != null) {
                    authEvent = (SerializableEvent) input.readObject();
                    System.out.println("SYSTEM: Auth received.");

                    if (authEvent instanceof AuthSucceedEvent) {
                        AuthSucceedEvent authSuccess = (AuthSucceedEvent) authEvent;

                        this.user = authSuccess.getUser();
                        this.channels = authSuccess.getChannels();
                        this.friends = authSuccess.getFriends();
                        this.user.setStatus(User.ONLINE);

                        System.out.println("SYSTEM: current channels: " + channels.size());

                        this.currentlyLoggingIn = false;
                        System.out.println("SYSTEM: login succeeded!");
                    } else {
                        loginWindow.reload();
                        System.out.println("SYSTEM: login failed.");
                    }
                }
            } catch (IOException e) {
                System.out.println("SYSTEM: A connection issue occured.");
                if (this.servSocket != null) {
                    try {
                        this.closeEverything();
                    } catch (IOException e2) {
                        System.out.println("SYSTEM: Failed to close everything.");
                    }
                }

                System.exit(1);
            } catch (ClassNotFoundException e) {
                System.out.println("SYSTEM: An error occured while reading from the server.");
            }
        }

        // We no longer need the login window
        loginWindow.destroy();
    }

    /**
     * Launches a thread worker that attempts to connect to the server. This thread
     * will continue to attempt connections until a connection happens or the
     * application is killed.
     * <p>
     * The design of this method also allows for ip and port to be changed, and for
     * the changes to immediately be reflected here.
     */
    public void attemptConnection() {
        if (servSocket != null) {
            return;
        }

        Thread connectorWorker = new Thread(new Runnable() {
            public synchronized void run() {
                boolean connected = false;
                while (!connected) {
                    if (ip.equals("")) {
                        continue;
                    }

                    connected = connect(ip, port);

                    if (!connected) {
                        System.out.println("SYSTEM: Error connecting.");

                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e2) {
                            System.out.println("SYSTEM: Error waiting.");
                        }
                    }
                }

                System.out.println("SYSTEM: Connection made.");
            }
        });

        connectorWorker.start();
    }

    /**
     * Initializes a thread worker that takes outgoing events from the outgoing
     * event queue and sends them to the server.
     * <p>
     * This ensures that all outputs are at one location, to prevent stream
     * conflicts and synchronizations.
     */
    private void initializeOutgoingEventWorker() {
        Thread outgoingEventWorker = new Thread(new Runnable() {
            public synchronized void run() {
                while (running) {
                    synchronized (outgoingEvents) {
                        if (servSocket != null) {
                            try {
                                // A blocking call
                                SerializableEvent event = outgoingEvents.take();

                                output.writeObject(event);
                                output.flush();
                                output.reset();
                            } catch (EOFException e) {
                                System.out.println("SYSTEM: End of output stream.");
                            } catch (InterruptedException e) {
                                System.out.println("SYSTEM: queue was interrupted while blocking.");
                            } catch (IOException e) {
                                System.out.println("SYSTEM: could not send an event.");
                            }
                        }
                    }
                }
            }
        });

        outgoingEventWorker.start();
    }

    /**
     * Attempts to connect to the specified IP at the specified port.
     * <p>
     * Returns a boolean success value.
     * 
     * @param ip   the it to connect to.
     * @param port the port to connect to.
     * @return true if connection succeeded.
     */
    public boolean connect(String ip, int port) {
        System.out.println("SYSTEM: Attempting to connect to central servers...");

        try {
            this.servSocket = new Socket(ip, port);

            input = new ObjectInputStream(servSocket.getInputStream());
            output = new ObjectOutputStream(servSocket.getOutputStream());
        } catch (IOException e) { // connection error occured
            System.out.println("SYSTEM: Connection to server failed.");
            return false;
        }

        return true;
    }

    /**
     * Retrieves this client's associated user.
     * 
     * @return a {@code User} object with this client's user.
     */
    public User getUser() {
        return this.user;
    }

    /**
     * Retrieves this client's channels.
     * 
     * @return a {@code HashMap} with this client's channels.
     */
    public HashMap<Integer, Channel> getChannels() {
        return this.channels;
    }

    /**
     * Retrieves this client's friends.
     * 
     * @return a {@code HashMap} with this client's friends.
     */
    public HashMap<String, User> getFriends() {
        return this.friends;
    }

    /**
     * Checks whether this client has a current channel or not.
     * 
     * @return true if this client has a current channel.
     */
    public boolean hasCurrentChannel() {
        return this.currentChannel != null;
    }

    /**
     * Retrieves this client's current channel.
     * 
     * @return a {@code Channel} object with this client's current channel.
     */
    public Channel getCurrentChannel() {
        return this.currentChannel;
    }

    /**
     * Sets this client's current channel to a provided current channel.
     * <p>
     * As channels are not necessarily linked or pointing to each other, this method
     * is inheritly dangerous. If the channel cannot be confirmed to be a reference
     * of an existing channel in this client's list of channels, try using
     * {@link #setCurrentChannel(int) the other set method} instead.
     * 
     * @param currentChannel the new current channel.
     */
    public void setCurrentChannel(Channel currentChannel) {
        this.currentChannel = currentChannel;
    }

    public void setCurrentChannel(int channelId) {
        this.currentChannel = this.channels.get(channelId);
    }

    /**
     * Retrieves this client's main menu frame.
     * 
     * @return this client's main menu frame.
     */
    public MainFrame getMainMenuFrame() {
        return this.mainMenu;
    }

    /**
     * Retrieves this client's login frame.
     * 
     * @return this client's login frame.
     */
    public LoginFrame getLoginFrame() {
        return this.loginWindow;
    }

    /**
     * Ensures that this client has an initialized, visible login window.
     * <p>
     * Only important if the login window's existance cannot be guaranteed.
     * 
     * @return true if this client has an initialized, visible login window.
     */
    public boolean hasLoginFrame() {
        return (this.loginWindow != null && this.loginWindow.isVisible());
    }

    /**
     * Ensures that this client has an initialized, visible main window.
     * <p>
     * Only important if the main menu's existance cannot be guaranteed.
     * 
     * @return true if this client has an initialized, visible main window.
     */
    public boolean hasMainMenuFrame() {
        return (this.mainMenu != null && this.mainMenu.isVisible());
    }

    /**
     * Retrieves this client's connecting IP.
     * 
     * @return this client's connecting IP.
     */
    public String getIp() {
        return this.ip;
    }

    /**
     * Sets this client's connecting IP.
     * 
     * @param ip this client's new connecting IP.
     */
    public void setIp(String ip) {
        this.ip = ip;
    }

    /**
     * Retrieves this client's connecting port.
     * 
     * @return this client's connecting port.
     */
    public int getPort() {
        return this.port;
    }

    /**
     * Sets this client's connecting port.
     * 
     * @param ip this client's new connecting port.
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Offers an event to this client's output queue.
     * 
     * @param event the event to be enqueued.
     */
    public synchronized void offerEvent(SerializableEvent event) {
        this.outgoingEvents.offer(event);
    }

    /**
     * Spawns a worker thread that saves the client's current ip settings.
     * <p>
     * These settings will be written directly to "data/".
     */
    public synchronized void saveIpSettings() {
        Thread saveIpSettingWorker = new Thread(new Runnable() {
            public void run() {
                try {
                    File ipSettings = new File("data/ipconfig");

                    if (!ipSettings.exists()) {
                        ipSettings.createNewFile();
                    }

                    FileWriter writer = new FileWriter(ipSettings);
                    writer.write(ip + "\n" + Integer.toString(port) + "\n");
                    writer.close();

                    System.out.println("SYSTEM: IP settings saved!.");
                } catch (IOException e) {
                    System.out.println("SYSTEM: Could not save IP settings.");
                }
            }
        });
        saveIpSettingWorker.start();
    }

    /**
     * Safely closes this client and its associated socket.
     * <p>
     * This method should be used whenever this client must be closed, and will
     * ensure that any to-be-outgoing events will be served before closing.
     */
    private synchronized void closeSafely() {
        while (!hasClosed) {
            // Make sure all outgoing events are served
            synchronized (outgoingEvents) {
                if (this.outgoingEvents.peek() == null) {
                    this.running = false;
                    try {
                        // Close all JFrames
                        closeEverything();
                        System.out.println("SYSTEM: closed socket.");
                    } catch (IOException e) {
                        System.out.println("SYSTEM: Failed to close socket.");
                    }

                    hasClosed = true;
                }
            }
        }
    }

    /**
     * Attempts to close all sockets and all frames.
     * 
     * @throws IOException if closing the sockets/frames throws an I/O error.
     */
    private void closeEverything() throws IOException {
        if (this.hasMainMenuFrame()) {
            this.mainMenu.destroy();
        }

        input.close();
        output.close();
        servSocket.close();
    }

    /**
     * Starts the client shutdown process.
     * <p>
     * This method should be used to signal that this client should begin shutting
     * down. If a forced shutdown is required because the client cannot guarantee a
     * safe shutdown, (eg. the client {@link #start() start loop} to detect events
     * has not begun), use {@link #forceLogout()}
     */
    public synchronized void initiateShutdown() {
        this.logout();
    }

    /**
     * Safely closes this client.
     * <p>
     * This method will ensure that all queued outgoing events are properly served
     * and that all connections are closed before closing.
     */
    private synchronized void logout() {
        if (!currentlyLoggingIn && !hasClosed) {
            outgoingEvents.offer(new ClientStatusUpdateEvent(this.user.getUsername(), 0));
        }
    
        this.closeSafely();
    }

    /**
     * Closes this client.
     * <p>
     * This method does NOT guarantee that outgoing events are served. This method
     * is designed to close everything and then exit the system. Use this method if
     * the {@link #start()} loop has failed, and a safe shutdown from that method is
     * not guaranteed.
     * <p>
     * If the program has executed correctly and the {@link #start()} loop has not
     * failed, use {@link #initiateShutdown()} for a safer shutdown method.
     * 
     * @param status the status for the system exit.
     */
    public synchronized void forceLogout(int status) {
        try {
            this.closeEverything();
        } catch (IOException e) {
            System.out.println("SYSTEM: Failed to close socket.");
        }

        System.exit(status);
    }
}