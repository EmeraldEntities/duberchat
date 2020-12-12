package duberchat.client;

import java.io.*;
import java.net.*;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.HashMap;

import duberchat.chatutil.*;
import duberchat.events.*;
import duberchat.gui.frames.*;
import duberchat.handlers.*;
import duberchat.handlers.client.*;

/* [ChatClient.java]
 * A not-so-pretty implementation of a basic chat client
 * @author Mangat
 * @ version 1.0a
 */

public class ChatClient {
    private String ip = "";
    private int port = -1;

    private Socket servSocket;
    private User user;
    private HashMap<Integer, Channel> channels;
    private HashMap<Class<? extends SerializableEvent>, Handleable> eventHandlers;

    private Channel currentChannel;

    private ObjectInputStream input;
    private ObjectOutputStream output;

    private boolean running = true; // thread status via boolean
    private boolean currentlyLoggingIn = true;
    private boolean hasClosed = false;

    private ConcurrentLinkedQueue<SerializableEvent> outgoingEvents;

    private LoginFrame loginWindow;
    private MainFrame mainMenu;

    public void start() {
        // call a method that connects to the server
        this.outgoingEvents = new ConcurrentLinkedQueue<>();
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
                System.out.println("SYSTEM: Handling event " + newEvent);

                this.eventHandlers.get(newEvent.getClass()).handleEvent(newEvent);
            } catch (EOFException e) {
                System.out.println("SYSTEM: End of stream.");
            } catch (IOException e) {
                System.out.println("SYSTEM: Failed to obtain an event from the server.");
            } catch (ClassNotFoundException e) {
                System.out.println("SYSTEM: Attempted to handle unknown event.");
            }
        }

        this.logout();
    }

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

    private void initializeHandlers() {
        this.eventHandlers = new HashMap<>();

        this.eventHandlers.put(ChannelAddMemberEvent.class, new ClientChannelAddMemberHandler(this));
        this.eventHandlers.put(ChannelRemoveMemberEvent.class, new ClientChannelRemoveMemberHandler(this));

        this.eventHandlers.put(ClientProfileUpdateEvent.class, new ClientProfileUpdateHandler(this));
        this.eventHandlers.put(ClientRequestMessageEvent.class, new ClientRequestMessageHandler(this));
        this.eventHandlers.put(ChannelCreateEvent.class, new ClientChannelCreateHandler(this));
        this.eventHandlers.put(ChannelDeleteEvent.class, new ClientChannelDeleteHandler(this));
        this.eventHandlers.put(RequestFailedEvent.class, new ClientRequestFailedHandler(this));

        this.eventHandlers.put(MessageSentEvent.class, new ClientMessageSentHandler(this));
        this.eventHandlers.put(MessageEditEvent.class, new ClientMessageEditHandler(this));
        this.eventHandlers.put(MessageDeleteEvent.class, new ClientMessageDeleteHandler(this));
    }

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
            } catch (ClassNotFoundException e) {
                System.out.println("SYSTEM: An error occured while reading from the server.");
            }
        }

        // We no longer need the login window
        loginWindow.destroy();
    }

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

    private void initializeOutgoingEventWorker() {
        Thread outgoingEventWorker = new Thread(new Runnable() {
            public synchronized void run() {
                while (running) {
                    synchronized (outgoingEvents) {
                        if (servSocket != null && outgoingEvents.peek() != null) {
                            System.out.println("SYSTEM: logged event in queue.");
                            SerializableEvent event = outgoingEvents.remove();
                            System.out.println(event);
                            try {
                                output.writeObject(event);
                                output.flush();
                                System.out.println("SYSTEM: sent event.");
                            } catch (IOException e) {
                                System.out.println("SYSTEM: Could not send a " + event.getClass().toString());
                            }
                        }
                    }
                }
            }
        });

        outgoingEventWorker.start();
    }

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
     */
    public synchronized void offerEvent(SerializableEvent event) {
        this.outgoingEvents.offer(event);
    }

    /**
     * Spawns a worker thread that saves the client's current ip settings.
     * <p>
     * These settings will be written directly to "data/".
     */
    public void saveIpSettings() {
        Thread saveIpSettingWorker = new Thread(new Runnable() {
            public void run() {
                try {
                    File ipSettings = new File("data/ipconfig");

                    if (!ipSettings.exists()) {
                        ipSettings.createNewFile();
                    }

                    FileWriter writer = new FileWriter(ipSettings);
                    writer.write(ip + "\n" + Integer.toString(port) + "\n"); // TODO: replace with buffered?
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
    private void closeSafely() {
        while (!hasClosed) {
            // Make sure all outgoing events are served
            synchronized (outgoingEvents) {
                if (this.outgoingEvents.peek() == null) {
                    this.running = false;
                    try {
                        input.close();
                        output.close();
                        servSocket.close();
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
     * Starts the client shutdown process.
     * <p>
     * This method should be used to signal that this client should begin shutting
     * down. If a forced shutdown is required because the client cannot guarantee a
     * safe shutdown, (eg. the client {@link #start() start loop} to detect events
     * has not begun), use {@link #forceLogout()}
     */
    public void initiateShutdown() {
        this.logout();
    }

    /**
     * Safely closes this client.
     * <p>
     * This method will ensure that all queued outgoing events are properly served
     * and that all connections are closed before closing.
     */
    private void logout() {
        if (!currentlyLoggingIn) {
            User newUser = new User(this.user, 0);
            outgoingEvents.offer(new ClientProfileUpdateEvent(newUser));
        }
    
        this.closeSafely();
    }

    /**
     * Closes this client.
     * <p>
     * This method ensures that all outgoing queued events are properly served and
     * that all connections are closed, and closes the system from within this
     * method. Use this method if the {@link #start()} loop has failed, and a safe
     * shutdown from that method is not guaranteed.
     * <p>
     * If the program has executed correctly and the {@link #start()} loop has not
     * failed, use {@link #initiateShutdown()} for a safer shutdown method.
     */
    public void forceLogout() {
        this.logout();

        System.exit(0);
    }
}