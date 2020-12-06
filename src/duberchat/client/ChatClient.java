package duberchat.client;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.net.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Scanner;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.HashMap;

import duberchat.chatutil.*;
import duberchat.events.*;
import duberchat.frames.*;
import duberchat.handlers.*;
import duberchat.handlers.client.*;

/* [ChatClient.java]
 * A not-so-pretty implementation of a basic chat client
 * @author Mangat
 * @ version 1.0a
 */

public class ChatClient {
    private Socket servSocket;
    private User user;
    private HashMap<Integer, Channel> channels;
    private HashMap<Class<? extends SerializableEvent>, Handleable> eventHandlers;

    private Channel currentChannel;

    private ObjectInputStream input;
    private ObjectOutputStream output;

    private boolean running = true; // thread status via boolean
    private boolean currentlyLoggingIn = true;

    private Scanner console; // TODO: remove, this is temp
    private ConcurrentLinkedQueue<SerializableEvent> outgoingEvents;

    private LoginFrame loginWindow;
    private MainMenuFrame mainMenu;

    public void start() {
        // call a method that connects to the server
        this.initializeHandlers();
        this.outgoingEvents = new ConcurrentLinkedQueue<>();

        this.initializeConnectionWorker();
        this.initializeOutgoingEventWorker();

        this.login();

        mainMenu = new MainMenuFrame("duberchat", this, this.outgoingEvents);
        mainMenu.setVisible(true);

        while (running) {
            // a blocking call
            try {
                SerializableEvent newEvent = (SerializableEvent) input.readObject();
                System.out.println("SYSTEM: Handling event " + newEvent);

                this.eventHandlers.get(newEvent.getClass()).handleEvent(newEvent);
            } catch (IOException e) {
                System.out.println("SYSTEM: Failed to obtain an event from the server.");
            } catch (ClassNotFoundException e) {
                System.out.println("SYSTEM: Attempted to handle unknown event.");
            }
        }

        // testMessages(console);
        this.closeSafely();
    }

    private void initializeHandlers() {
        this.eventHandlers = new HashMap<>();

        this.eventHandlers.put(ChannelCreateEvent.class, new ClientChannelCreateHandler(this));
        this.eventHandlers.put(RequestFailedEvent.class, new ClientRequestFailedHandler(this));
        this.eventHandlers.put(MessageSentEvent.class, new ClientMessageSentHandler(this));
    }

    private void login() {
        loginWindow = new LoginFrame(this, outgoingEvents);
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

    private void initializeConnectionWorker() {
        Thread connectorWorker = new Thread(new Runnable() {
            public synchronized void run() {
                boolean connected = false;
                while (!connected) {
                    connected = connect("127.0.0.1", 6969);

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
            this.servSocket = new Socket("127.0.0.1", port);

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
     * Retrieves this client's current channel.
     * 
     * @return a {@code Channel} object with this client's current channel.
     */
    public Channel getCurrentChannel() {
        return this.currentChannel;
    }

    /**
     * Sets this client's current channel to a provided current channel.
     * 
     * @param currentChannel the new current channel.
     */
    public void setCurrentChannel(Channel currentChannel) {
        this.currentChannel = currentChannel;
    }

    /**
     * Retrieves this client's main menu frame.
     * 
     * @return this client's main menu frame.
     */
    public MainMenuFrame getMainMenuFrame() {
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
     * Safely closes this client and its associated socket.
     * <p>
     * This method should be used whenever this client must be closed, and will
     * ensure that any to-be-outgoing events will be served before closing.
     */
    private void closeSafely() {
        outgoingEvents.offer(new ClientStatusUpdateEvent(this, User.OFFLINE));

        boolean hasClosed = false;
        while (!hasClosed) {
            // Make sure all outgoing events are served
            synchronized (outgoingEvents) {
                if (this.outgoingEvents.peek() == null) {
                    this.running = false;
                    try {
                        input.close();
                        output.close();
                        servSocket.close();

                        System.out.println("Closed socket.");
                    } catch (Exception e) {
                        System.out.println("Failed to close socket.");
                    }

                    hasClosed = true;
                }
            }
        }
    }

    /**
     * Safely closes this client.
     * <p>
     * This method will ensure that all queued outgoing events are properly served
     * and that all connections are closed before closing.
     */
    public void logout() {
        if (!currentlyLoggingIn) {
            outgoingEvents.offer(new ClientStatusUpdateEvent(this, User.OFFLINE));
        }

        this.closeSafely();
        System.exit(0);
    }
}