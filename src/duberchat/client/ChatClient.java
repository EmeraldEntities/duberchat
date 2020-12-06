package duberchat.client;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.net.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.HashMap;

import duberchat.chatutil.*;
import duberchat.events.*;
import duberchat.frames.*;

/* [ChatClient.java]
 * A not-so-pretty implementation of a basic chat client
 * @author Mangat
 * @ version 1.0a
 */

public class ChatClient {
    private Socket servSocket;
    private User user;
    HashMap<Integer, Channel> channels;

    private ObjectInputStream input;
    private ObjectOutputStream output;

    private boolean running = true; // thread status via boolean

    private Scanner console; // TODO: remove, this is temp
    private ConcurrentLinkedQueue<SerializableEvent> outgoingEvents;

    public void start() {
        // call a method that connects to the server
        this.outgoingEvents = new ConcurrentLinkedQueue<>();

        this.initializeConnectionWorker();
        this.initializeOutgoingEventWorker();

     // try {
     //     Thread.sleep(3000);
     // } catch (InterruptedException e1) {
     //     // TODO Auto-generated catch block
     //     e1.printStackTrace();
     // }
     // try {
     //     this.user = new User("boop");
     //     ChannelCreateEvent test = new ChannelCreateEvent(this, null, null);
     //     output.writeObject(test);
     // } catch (IOException e) {
     //     e.printStackTrace();
     // }

        Scanner console = new Scanner(System.in);

        this.login();


        // TODO: TEMP
        Channel newChannel;
        while (true) {
            try {

                HashSet<String> usernames = new HashSet<>();
                usernames.add("EmeraldPhony");
                outgoingEvents.offer(new ChannelCreateEvent(this.user, null, usernames));
                SerializableEvent incoming = (SerializableEvent) input.readObject();

                if (incoming instanceof RequestFailedEvent) {
                    System.out.println("request failed");
                } else {
                    newChannel = ((ChannelCreateEvent) incoming).getChannel();
                    System.out.println("Channel created!");
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // TODO: TEMP
        while (true) {
            String str = console.nextLine();
            if (str.equals("quit"))
                break;

            outgoingEvents.offer(new MessageSentEvent(this, new Message(str, this.user.getUsername(), -1, newChannel)));

            try {
                SerializableEvent response = (SerializableEvent) input.readObject();

                if (response instanceof MessageSentEvent) {
                    Message msg = ((MessageSentEvent) response).getMessage();
                    System.out.println("Received message " + msg.getMessage() + " with user " + msg.getSenderUsername()
                            + " with an id of " + msg.getMessageId());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        console.close();
        this.closeSafely();
    }

    private void login() {
        boolean currentlyLoggingIn = true;

        LoginScreen loginWindow = new LoginScreen(this, outgoingEvents);
        loginWindow.setVisible(true);

        SerializableEvent authEvent;
        while (currentlyLoggingIn) {
            try {
                if (this.servSocket != null) {
                    authEvent = (SerializableEvent) input.readObject();
                    System.out.println("SYSTEM: Auth received.");

                    if (authEvent instanceof AuthSucceedEvent) {
                        AuthSucceedEvent authSuccess = (AuthSucceedEvent) authEvent;

                        this.user = authSuccess.getUser();
                        this.channels = authSuccess.getChannels();

                        currentlyLoggingIn = false;
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
                while (true) {
                    // if (outgoingEvents.peek() != null)
                    // System.out.println("Event present at top of queue!");
                    synchronized (outgoingEvents) {
                        if (servSocket != null && outgoingEvents.peek() != null) {
                            System.out.println("SYSTEM: logged event in queue.");
                            SerializableEvent event = outgoingEvents.remove();
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

    public void readMessagesFromServer() {
        while (running) {
            try {
                if (input.available() > 0) {
                    Event event = (Event) input.readObject();
                    System.out.println("received a " + event.getClass().toString() + "obj");
                }

            } catch (IOException e) {
                System.out.println("Failed to receive msg from the server");
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                System.out.println("A loaded event could not be identified.");
            }
        }

        // close the sockets
        closeSafely();
    }

    private void closeSafely() {
        outgoingEvents.offer(new ClientStatusUpdateEvent(this, User.OFFLINE));

        boolean hasClosed = false;
        while (!hasClosed) {
            // Make sure all outgoing events are served
            if (this.outgoingEvents.peek() == null) {
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
    
    public User getUser() {
        return this.user;
    }

}