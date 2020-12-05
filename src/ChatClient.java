/* [ChatClient.java]
 * A not-so-pretty implementation of a basic chat client
 * @author Mangat
 * @ version 1.0a
 */

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.net.*;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashSet;
import java.util.Scanner;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.HashMap;

import chatutil.*;
import events.*;

class ChatClient {
    private Socket servSocket;
    User user;
    HashMap<Integer, Channel> channels;

    private ObjectInputStream input;
    private ObjectOutputStream output;

    private boolean running = true; // thread status via boolean

    private Scanner console; // TODO: remove, this is temp
    private ConcurrentLinkedQueue<EventObject> outgoingEvents;

    public static void main(String[] args) {
        new ChatClient().start();
    }

    public void start() {
        // call a method that connects to the server
        this.outgoingEvents = new ConcurrentLinkedQueue<>();

        this.initializeConnectionWorker();
        this.initializeOutgoingEventWorker();

        Scanner console = new Scanner(System.in);

        this.login();

        console.nextLine();
        console.close();
        this.closeSafely();
    }

    private void login() {
        boolean currentlyLoggingIn = true;

        LoginScreen loginWindow = new LoginScreen(this, outgoingEvents);
        loginWindow.setVisible(true);

        EventObject authEvent;
        while (currentlyLoggingIn) {
            try {
                if (this.servSocket != null) {
                    authEvent = (EventObject) input.readObject();
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
                    if (servSocket != null && outgoingEvents.peek() != null) {
                        System.out.println("SYSTEM: logged event in queue.");
                        EventObject event = outgoingEvents.remove();
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
                    EventObject event = (EventObject) input.readObject();
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
}