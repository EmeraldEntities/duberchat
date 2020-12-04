/* [ChatServer.java]
 * You will need to modify this so that received messages are broadcast to all clients
 * @author Mangat
 * @ version 1.0a
 */

//imports for network communication
import java.io.*;
import java.net.*;
import java.util.EventObject;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import events.*;
import chatutil.*;

public class ChatServer {

    ServerSocket serverSock;// server socket for connection
    static boolean running = true; // controls if the server is accepting clients
    HashMap<Integer, User> users; // maps user ID to user
    HashMap<String, String> textConversions; // For text commands
    HashMap<Integer, Channel> channels; // channel id to list of all online users in channel
    EventHandlerThread eventsThread;

    /**
     * Main
     * 
     * @param args parameters from command line
     */
    public static void main(String[] args) {
        new ChatServer().go(); // start the server
    }

    /**
     * Go Starts the server
     */
    public void go() {
        System.out.println("Waiting for a client connection..");

        Socket client = null; // hold the client connection

        try {
            serverSock = new ServerSocket(6969); // assigns an port to the server
            // serverSock.setSoTimeout(15000); // 15 second timeout
            eventsThread = new EventHandlerThread(new EventHandler(channels));
            eventsThread.start();
            while (running) { // this loops to accept multiple clients
                client = serverSock.accept(); // wait for connection
                System.out.println("Client connected");
                // Note: you might want to keep references to all clients if you plan to
                // broadcast messages
                // Also: Queues are good tools to buffer incoming/outgoing messages
                Thread t = new Thread(new ConnectionHandler(client)); // create a thread for the new client and pass in
                                                                      // the
                // socket
                t.start(); // start the new thread
            }
        } catch (Exception e) {
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

    // ***** Inner class - thread for client connection
    class ConnectionHandler implements Runnable {
        private ObjectOutputStream output; // assign printwriter to network stream
        private ObjectInputStream input; // Stream for network input
        private Socket client; // keeps track of the client socket
        private boolean running;

        /*
         * ConnectionHandler Constructor
         * 
         * @param the socket belonging to this client connection
         */
        ConnectionHandler(Socket s) {
            this.client = s; // constructor assigns client to this
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
            EventObject obj;

            // Send a message to the client

            // Get a message from the client
            while (running) { // loop unit a message is received
                try {
                    obj = (EventObject) input.readObject(); // get a message from the client
                    System.out.println("Received a message");
                    eventsThread.addEvent(obj);
                    output.flush();
                } catch (IOException e) {
                    System.out.println("Failed to receive msg from the client");
                    e.printStackTrace();
                } catch (ClassNotFoundException e1) {
                    System.out.println("Class not found :(");
                    e1.printStackTrace();
                }
            }

            // Send a message to the client
//            output.println("We got your message! Goodbye.");
//            output.flush();

            // close the socket
            try {
                input.close();
                output.close();
                client.close();
            } catch (Exception e) {
                System.out.println("Failed to close socket");
            }
        } // end of run()
    } // end of inner class

    /**
     * [EventHandler] Thread target.
     * 
     * @author Paula Yuan
     * @version 0.1
     */
    public class EventHandler implements Runnable {
        private ConcurrentLinkedQueue<EventObject> eventQueue;
        private HashMap<Integer, Channel> channels;

        /**
         * [EventHandler] Constructor for the events handler.
         * 
         * @param channels HashMap<Integer, Channel>, a map of all the channel ids to a
         *                 list of all their online users' threads
         */
        public EventHandler(HashMap<Integer, Channel> channels) {
            this.channels = channels;
            this.eventQueue = new ConcurrentLinkedQueue<EventObject>();
        }

        /**
         * run Executed when the thread starts
         */
        public void run() {
            while (true) {
                EventObject event = this.eventQueue.poll();
                if (event == null)
                    continue;
                if (event instanceof ClientStatusUpdateEvent) {
                    System.out.println("status update event");
                } else if (event instanceof ClientLoginEvent) {
                    System.out.println("client login event");
                } else if (event instanceof ClientRequestMessageEvent) {
                    System.out.println("client request message event");
                } else if (event instanceof MessageSentEvent) {
                    System.out.println("message sent event");
                } else if (event instanceof MessageDeleteEvent) {
                    System.out.println("message delete event");
                } else if (event instanceof MessageEditEvent) {
                    System.out.println("message edit event");
                }
            }
        }

        /**
         * getEventQueue Returns the event queue.
         * 
         * @return ConcurrentLinkedQueue<EventObject> eventQueue, the event queue
         */
        public ConcurrentLinkedQueue<EventObject> getEventQueue() {
            return this.eventQueue;
        }

    } // end of inner class

    /**
     * [EventHandlerThread] Thread for handling all events for server-client
     * interaction.
     * 
     * @author Paula Yuan
     * @version 0.1
     */
    public class EventHandlerThread extends Thread {
        private EventHandler target;

        /**
         * [EventHandlerThread] Constructor for a new event handler thread.
         * 
         * @param target Runnable, the target object
         */
        public EventHandlerThread(EventHandler target) {
            super(target);
            this.target = target;
        }

        /**
         * [addEvent] Adds an event to the event queue.
         * 
         * @param event EventObject, the new event to add.
         */
        public void addEvent(EventObject event) {
            this.target.getEventQueue().add(event);
        }
    } // end of inner class
} // end of Class
