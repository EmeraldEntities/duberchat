package duberchat.frames;

import java.awt.event.*;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.concurrent.ConcurrentLinkedQueue;

import duberchat.events.*;
import sun.misc.resources.Messages;
import duberchat.client.ChatClient;

import duberchat.chatutil.Channel;
import duberchat.chatutil.Message;

@SuppressWarnings("serial")
public class MainMenuFrame extends DynamicFrame {
    public static final Dimension DEFAULT_SIZE = Toolkit.getDefaultToolkit().getScreenSize();
    private static final Color MAIN_COLOR = new Color(60, 60, 60);
    private static final Color TEXTBOX_COLOR = new Color(80, 80, 80);
    private static final Color TEXT_COLOR = new Color(150, 150, 150);
    private static final Color BRIGHT_TEXT_COLOR = new Color(220, 220, 220);

    private JPanel channelPanel;
    private JPanel userPanel; // the right one if applicable
    private JPanel typingPanel;
    private JPanel textPanel;

    private ChannelCreateFrame addChannelFrame;

    private JButton sendButton, addChannelButton, quitButton;
    private JTextField typeField;
    private JTextArea msgArea;

    private ConcurrentLinkedQueue<SerializableEvent> output;
    private ChatClient client;

    public MainMenuFrame(String title, ChatClient client, ConcurrentLinkedQueue<SerializableEvent> outgoingEvents) {
        super(title);
        this.client = client;
        this.output = outgoingEvents;

        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setSize(MainMenuFrame.DEFAULT_SIZE);
        this.setResizable(true);

        typingPanel = new JPanel();
        channelPanel = new JPanel();
        userPanel = new JPanel();

        channelPanel.setBackground(Color.BLUE);
        userPanel.setBackground(Color.GREEN);

        typingPanel.setLayout(new GridLayout(2, 0));
        channelPanel.setLayout(new BoxLayout(channelPanel, BoxLayout.PAGE_AXIS));

        sendButton = new JButton("SEND");
        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (client.getCurrentChannel() == null) {
                    return;
                }

                Message msg = new Message(typeField.getText(), client.getUser().getUsername(), -1,
                        client.getCurrentChannel());
                output.offer(new MessageSentEvent(client, msg));
                typeField.setText("");

                System.out.println("SYSTEM: Sent message " + typeField.getText());
            }
        });

        quitButton = new JButton("QUIT");
        quitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("SYSTEM: Exiting application!");

                client.logout();
                destroy();
            }
        });

        addChannelButton = new JButton("CREATE");
        addChannelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (hasActiveChannelCreateFrame()) {
                    return;
                }

                addChannelFrame = new ChannelCreateFrame(client, output);
                addChannelFrame.setVisible(true);
            }
        });

        typeField = new JTextField(20);
        typeField.setBackground(TEXTBOX_COLOR);
        typeField.setForeground(BRIGHT_TEXT_COLOR);

        msgArea = new JTextArea();
        msgArea.setEditable(false);
        msgArea.setBackground(MAIN_COLOR);
        msgArea.setForeground(BRIGHT_TEXT_COLOR);

        if (client.getCurrentChannel() == null) {
            typeField.setText("Send a message here...");
            typeField.setEditable(false);
        } else {
            typeField.setEditable(true);
        }

        typingPanel.add(typeField);
        typingPanel.add(sendButton);
        typingPanel.add(new JLabel(""));
        typingPanel.add(quitButton);

        channelPanel.add(addChannelButton);

        this.add(BorderLayout.WEST, channelPanel);
        this.add(BorderLayout.EAST, userPanel);
        this.add(BorderLayout.CENTER, msgArea);
        this.add(BorderLayout.SOUTH, typingPanel);
    }

    public void reload() {
        if (client.getCurrentChannel() != null) {
            typeField.setEditable(true);
            typeField.setText("");
        } else {
            typeField.setText("Send a message here...");
            typeField.setEditable(false);
        }
    }

    public boolean hasActiveChannelCreateFrame() {
        return (this.addChannelFrame != null && this.addChannelFrame.isVisible());
    }

    public ChannelCreateFrame getChannelCreateFrame() {
        return this.addChannelFrame;
    }

    public boolean closeChannelCreateFrame() {
        if (this.addChannelFrame == null) {
            return false;
        }

        this.addChannelFrame.destroy();
        return true;
    }
}
