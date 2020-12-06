package duberchat.frames;

import java.awt.event.*;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Arrays;

import duberchat.events.ChannelCreateEvent;
import duberchat.events.SerializableEvent;
import duberchat.client.ChatClient;
import duberchat.chatutil.Channel;

@SuppressWarnings("serial")
//TODO: this should be a controlled frame so maybe work it so that it doesnt need output
public class ChannelCreateFrame extends DynamicGridbagFrame {
    public static final Dimension DEFAULT_SIZE = new Dimension(400, 500);
    private static final Color MAIN_COLOR = new Color(60, 60, 60);
    private static final Color TEXTBOX_COLOR = new Color(40, 40, 40);
    private static final Color TEXT_COLOR = new Color(150, 150, 150);
    private static final Color BRIGHT_TEXT_COLOR = new Color(220, 220, 220);

    private ChatClient client;
    private ConcurrentLinkedQueue<SerializableEvent> output;

    JPanel mainPanel;
    JTextField nameField;
    JTextField usersField;
    JButton submitButton;

    GridBagLayout layout;
    GridBagConstraints constraints;

    private boolean alreadySentRequest = false;

    public ChannelCreateFrame(ChatClient client, ConcurrentLinkedQueue<SerializableEvent> output) {
        super("Add a new channel...");

        this.client = client;
        this.output = output;

        this.setResizable(false);
        this.setSize(ChannelCreateFrame.DEFAULT_SIZE);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        mainPanel = new JPanel();
        mainPanel.setBackground(MAIN_COLOR);

        layout = new GridBagLayout();
        constraints = new GridBagConstraints();

        mainPanel.setLayout(layout);
        mainPanel.setSize(this.getSize());

        nameField = new JTextField(20);
        nameField.setBackground(TEXTBOX_COLOR);
        nameField.setForeground(BRIGHT_TEXT_COLOR);
        nameField.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        usersField = new JTextField(40);
        usersField.setBackground(TEXTBOX_COLOR);
        usersField.setForeground(BRIGHT_TEXT_COLOR);
        usersField.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JLabel channelNameLabel = new JLabel("Channel Name:");
        JLabel userLabel = new JLabel("Invite users.");
        JLabel userDescriptionLabel = new JLabel("Only existing users will be invited.");
        JLabel userSyntaxLabel = new JLabel("Seperate users using a comma, and put @ before name.");
        JLabel userExampleLabel = new JLabel("(eg. @EmeraldPhony, @PolyEntities)");

        channelNameLabel.setForeground(BRIGHT_TEXT_COLOR);
        userLabel.setForeground(BRIGHT_TEXT_COLOR);
        userDescriptionLabel.setForeground(TEXT_COLOR);
        userSyntaxLabel.setForeground(TEXT_COLOR);
        userExampleLabel.setForeground(TEXT_COLOR);

        submitButton = new JButton("Start DuberChatting!");
        submitButton.addActionListener(new CreateChannelActionListener());
        submitButton.setBackground(TEXT_COLOR);
        submitButton.setForeground(MAIN_COLOR);

        addConstrainedComponent(channelNameLabel, mainPanel, layout, constraints, 0, 0, 1, 1,
                GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER, new Insets(0, 0, 0, 0));
        addConstrainedComponent(nameField, mainPanel, layout, constraints, 0, 1, 1, 1, GridBagConstraints.HORIZONTAL,
                GridBagConstraints.CENTER, new Insets(8, 0, 8, 0));
        addConstrainedComponent(userLabel, mainPanel, layout, constraints, 0, 2, 1, 1, GridBagConstraints.HORIZONTAL,
                GridBagConstraints.CENTER, new Insets(30, 0, 0, 0));
        addConstrainedComponent(userDescriptionLabel, mainPanel, layout, constraints, 0, 3, 1, 1,
                GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER, new Insets(4, 0, 4, 0));
        addConstrainedComponent(userSyntaxLabel, mainPanel, layout, constraints, 0, 4, 1, 1,
                GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER, new Insets(4, 0, 4, 0));
        addConstrainedComponent(userExampleLabel, mainPanel, layout, constraints, 0, 5, 1, 1,
                GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER, new Insets(4, 0, 8, 0));
        addConstrainedComponent(usersField, mainPanel, layout, constraints, 0, 6, 1, 2, GridBagConstraints.HORIZONTAL,
                GridBagConstraints.CENTER, new Insets(8, 0, 8, 0));
        addConstrainedComponent(submitButton, mainPanel, layout, constraints, 0, 8, 1, 1, GridBagConstraints.REMAINDER,
                GridBagConstraints.CENTER, new Insets(8, 0, 8, 0));

        this.add(mainPanel);
    }

    /**
     * Requests a reload for this frame.
     * <p>
     * This method should only be called if channel creation failed.
     */
    public void reload() {
        this.handleFailedEvent();
    }

    private void handleFailedEvent() {
        JLabel failedText = new JLabel("Failed. Add at least one existing user!");
        failedText.setForeground(Color.RED);

        addConstrainedComponent(failedText, mainPanel, layout, constraints, 0, 8, 1, 1, GridBagConstraints.REMAINDER,
                GridBagConstraints.CENTER, new Insets(8, 0, 8, 0));

        mainPanel.revalidate();
        mainPanel.repaint();

        alreadySentRequest = false;
    }

    private class CreateChannelActionListener implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            if (alreadySentRequest)
                return;

            String channelName = nameField.getText();
            String[] users = usersField.getText().replace("@", "").split(", *");

            System.out.println(Arrays.toString(users));
            HashSet<String> usernames = new HashSet<>();

            for (String user : users) {
                usernames.add(user);
            }

            Channel newChannel = new Channel(channelName);
            ChannelCreateFrame.this.output.offer(new ChannelCreateEvent(client.getUser(), newChannel, usernames));

            alreadySentRequest = true;
            System.out.println("SYSTEM: Created new channel event.");
        }
    }
}
