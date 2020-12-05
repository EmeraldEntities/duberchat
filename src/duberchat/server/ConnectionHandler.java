package duberchat.server;

import java.io.*;
import java.net.*;

/**
 * [ConnectionHandler] Thread for client connection
 * 
 * @author Mr. Mangat, Paula Yuan
 * @version 0.1
 */

public class ConnectionHandler implements Runnable {
    private PrintWriter output; // assign printwriter to network stream
    private BufferedReader input; // Stream for network input
    private Socket client; // keeps track of the client socket
    private boolean running;

    /**
     * [ConnectionHandler] Constructor for this connection handler
     * 
     * @param s Socket, the socket belonging to this client connection
     */
    ConnectionHandler(Socket s) {
        this.client = s; // constructor assigns client to this

        // assign all connections to client
        try {
            this.output = new PrintWriter(client.getOutputStream());
            InputStreamReader stream = new InputStreamReader(client.getInputStream());
            this.input = new BufferedReader(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        running = true;
    } // end of constructor

    /**
     * [run] Is executed on start of thread
     */
    public void run() {

        // Get a message from the client
        String msg = "";

        // Get a message for the client
        while (running) { // loop until a message is received
            try {
                if (input.ready()) { // check for an incoming message
                    msg = input.readLine(); // get a message from the client
                    System.out.println("Received: " + msg);
                    output.println(msg); // echo the message back to the client
                    output.flush();
                }
            } catch (IOException e) {
                System.out.println("Failed to receive msg from the client");
                e.printStackTrace();
            }
        }

        // Send a message to the client
        output.println("We got your message! Goodbye.");
        output.flush();

        // close the socket
        try {
            input.close();
            output.close();
            client.close();
        } catch (Exception e) {
            System.out.println("Failed to close socket");
        }
    } // end of run()
} // end of class
