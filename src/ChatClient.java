/* [ChatClient.java]
 * A not-so-pretty implementation of a basic chat client
 * @author Mangat
 * @ version 1.0a
 */

import java.awt.*;
import javax.swing.*;
import java.io.*;
import java.net.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashSet;

import chatutil.*;
import events.*;

class ChatClient {

    private JButton sendButton, clearButton;
    private JTextField typeField;
    private JTextArea msgArea;
    private JPanel southPanel;
    private Socket servSocket;

    private ObjectInputStream input;
    private ObjectOutputStream output;
    private boolean running = true; // thread status via boolean

    public static void main(String[] args) {
        new ChatClient().go();
    }

    public void go() {
        JFrame window = new JFrame("Chat Client");
        southPanel = new JPanel();
        southPanel.setLayout(new GridLayout(2, 0));

        sendButton = new JButton("SEND");
        sendButton.addActionListener(new SendButtonListener());
        clearButton = new JButton("QUIT");
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                running = false;
            }
        });

        JLabel errorLabel = new JLabel("");

        typeField = new JTextField(10);
        msgArea = new JTextArea();

        southPanel.add(typeField);
        southPanel.add(sendButton);
        southPanel.add(errorLabel);
        southPanel.add(clearButton);

        window.add(BorderLayout.CENTER, msgArea);
        window.add(BorderLayout.SOUTH, southPanel);

        window.setSize(600, 600);
        window.setVisible(true);

        // call a method that connects to the server
        // after connecting loop and keep appending[.append()] to the JTextArea
        connect("127.0.0.1", 5000);
        readMessagesFromServer();
    }

    // Attempts to connect to the server and creates the socket and streams
    public Socket connect(String ip, int port) {
        System.out.println("Attempting to make a connection..");

        try {
            servSocket = new Socket("127.0.0.1", 6969);

            input = new ObjectInputStream(servSocket.getInputStream());
            output = new ObjectOutputStream(servSocket.getOutputStream());
        } catch (IOException e) { // connection error occured
            System.out.println("Connection to Server Failed.");
            e.printStackTrace();
        }

        System.out.println("Connection made.");

        return servSocket;
    }

    // Starts a loop waiting for server input and then displays it on the textArea
    public void readMessagesFromServer() {
        while (running) { // loop unit a message is received
            try {
                if (input.available() > 0) { // check for an incoming messge
                    EventObject event = (EventObject) input.readObject(); // read the message
                    System.out.println("received a " + event.getClass().toString() + "obj");
                    // msgArea.append(msg + "\n");
                }

            } catch (IOException e) {
                System.out.println("Failed to receive msg from the server");
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                System.out.println("A loaded event could not be identified.");
            }
        }

        try { // after leaving the main loop we need to close all the sockets
            input.close();
            output.close();
            servSocket.close();

            System.out.println("Closed socket.");
        } catch (Exception e) {
            System.out.println("Failed to close socket.");
        }
    }
    // ****** Inner Classes for Action Listeners ****

    // send - send msg to server (also flush), then clear the JTextField
    class SendButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            // Send a message to the client
            Channel chn = new Channel("hi", 1, new ArrayList<User>(), new HashSet<User>());
            Message msg = new Message(typeField.getText(), -1, chn);

            try {
                output.writeObject(new MessageSentEvent(ChatClient.this, msg));
                output.flush();
            } catch (IOException e) {
                System.out.println("Message not sent.");
            }

            typeField.setText("");
        }
    }
}