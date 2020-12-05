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
    static int numUsers = new File("users").listFiles().length;
    static int numChannels = new File("channels").listFiles().length;
    ServerSocket serverSock;// server socket for connection
    static boolean running = true; // controls if the server is accepting clients
    // HashMap<Integer, User> users; // maps user ID to user
    HashMap<String, String> textConversions; // For text commands
    HashMap<Integer, Channel> channels; // channel id to list of all online users in channel
    HashMap<User, Thread> curUsers; // map of all the online users to their threads
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
                ConnectionHandlerThread t = new ConnectionHandlerThread(new ConnectionHandler(client));
                ChatServer.this.curUsers.put(t.getTarget().user, t);
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
            EventObject event;

            // Send a message to the client

            // Get a message from the client
            while (running) { // loop unit a message is received
                try {
                    event = (EventObject) input.readObject(); // get a message from the client
                    System.out.println("Received a message");
                    if (event instanceof ClientLoginEvent) {
                        handleLoginEvent((ClientLoginEvent) event);
                    }
                    eventsThread.addEvent(event);
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
                output.close();
                input.close();
                client.close();
            } catch (Exception e) {
                System.out.println("Failed to close socket");
            }
        } // end of run()


        private void handleLoginEvent(ClientLoginEvent event) {
            String username = event.getUsername();
            int hashedPassword = event.getHashedPassword();
            File userFile = new File(username + ".txt");

            // Case 1: new user
            if (event.getIsNewUser()) {
                boolean created = false;
                try {
                    created = userFile.createNewFile();
                    // If the username is already taken, send auth failed event
                    if (!created) {
                        output.writeObject(new AuthFailedEvent(ChatServer.this));
                        return;
                    }

                    FileWriter writer = new FileWriter(username + ".txt");
                    writer.write(numUsers + "\n");
                    writer.write(username + "\n");
                    writer.write(hashedPassword + "\n");
                    writer.write("default.png" + "\n");
                    writer.write(0 + "\n");
                    writer.close();
                    this.user = new User(username, numUsers);
                    output.writeObject(new AuthSucceedEvent(ChatServer.this, this.user, 
                                       new HashMap<Integer, Channel>()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }

            // Case 2: already registered user                                
            try {
                BufferedReader reader = new BufferedReader(new FileReader(userFile));
                int userId = Integer.parseInt(reader.readLine().trim());
                // skip over username and password lines
                // assumption is made that file was titled correctly (aka file title = username)
                reader.readLine();
                reader.readLine();
                String pfpPath = reader.readLine().trim();
                this.user = new User(username, userId, pfpPath);
                int numChannels = Integer.parseInt(reader.readLine().trim());
                HashMap<Integer, Channel> userChannels = new HashMap<>();
                for (int i = 0; i < numChannels; i++) {
                    int channelId = Integer.parseInt(reader.readLine().trim());
                    userChannels.put(channelId, ChatServer.this.channels.get(channelId));
                }
                output.writeObject(new AuthSucceedEvent(ChatServer.this, this.user, userChannels));
                reader.close();
            } catch (FileNotFoundException e) {
                try {
                    output.writeObject(new AuthFailedEvent(ChatServer.this));
                } catch (IOException e2) {                           
                    e2.printStackTrace();
                } 
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    } // end of inner class

    class ConnectionHandlerThread extends Thread {
        private ConnectionHandler target;

        /**
         * [ConnectionHandlerThread] 
         * Constructor for a new connection handler thread.
         * @param target Runnable, the target object
         */
        public ConnectionHandlerThread(ConnectionHandler target) {
            super(target);
            this.target = target;
        }

        public ConnectionHandler getTarget() {
            return this.target;
        }
    }

    /**
     * [EventHandler] Thread target.
     * 
     * @author Paula Yuan
     * @version 0.1
     */
    class EventHandler implements Runnable {
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
                } else if (event instanceof ClientRequestMessageEvent) {
                    System.out.println("client request message event");
                } else if (event instanceof MessageSentEvent) {
                    System.out.println("message sent event");
                } else if (event instanceof MessageDeleteEvent) {
                    System.out.println("message delete event");
                } else if (event instanceof MessageEditEvent) {
                    System.out.println("message edit event");
                } else if (event instanceof ChannelRemoveEvent) {
                } else if (event instanceof ChannelCreateEvent) {
                } else if (event instanceof ChannelAddEvent) {
                } else if (event instanceof ChannelDeleteEvent) {
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
     * EventHandlerThread Thread for handling all events for server-client
     * interaction.
     * 
     * @author Paula Yuan
     * @version 0.1
     */
    public class EventHandlerThread extends Thread {
        private EventHandler target;

        /**
         * [EventHandlerThread] 
         * Constructor for a new event handler thread.
         * @param target Runnable, the target object
         */
        public EventHandlerThread(EventHandler target) {
            super(target);
            this.target = target;
        }

        /**
         * [addEvent] 
         * Adds an event to the event queue.
         * @param event EventObject, the new event to add.
         */
        public void addEvent(EventObject event) {
            this.target.getEventQueue().add(event);
        }
    } // end of inner class
} // end of Class
