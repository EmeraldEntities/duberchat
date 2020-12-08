package duberchat.frames;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

import java.util.EventObject;
import java.util.concurrent.ConcurrentLinkedQueue;

import duberchat.events.*;
import duberchat.client.ChatClient;
import duberchat.frames.filters.TextLengthFilter;

@SuppressWarnings("serial")
public class LoginFrame extends DynamicGridbagFrame {
    public static final Dimension DEFAULT_SIZE = new Dimension(400, 500);

    JPanel mainPanel;
    // JPanel contentPanel;

    JTextField usernameField;
    JPasswordField passwordField;
    JCheckBox newUserCheckbox;
    JButton submitButton;
    JButton optionsButton;
    GridBagLayout loginLayout;
    GridBagConstraints constraints;

    JLabel connectingText;
    JLabel failedText;

    ChatClient client;
    LoginSettingFrame settingsFrame;
    ConcurrentLinkedQueue<SerializableEvent> output;

    boolean alreadySentRequest = false;

    public LoginFrame(ChatClient client, ConcurrentLinkedQueue<SerializableEvent> outgoingEvents) {
        super("DuberChat");

        this.client = client;
        this.output = outgoingEvents;

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(LoginFrame.DEFAULT_SIZE);
        this.setResizable(false);
        this.setUndecorated(true);

        loginLayout = new GridBagLayout();
        constraints = new GridBagConstraints();

        mainPanel = new JPanel();
        mainPanel.setSize(this.getSize());
        mainPanel.setBackground(MainMenuFrame.MAIN_COLOR);
        mainPanel.setLayout(loginLayout);

        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width / 2 - this.getSize().width / 2, dim.height / 2 - this.getSize().height / 2);

        // =============================================

        usernameField = new JTextField(20);
        usernameField.setBackground(MainMenuFrame.SIDE_COLOR);
        usernameField.setForeground(MainMenuFrame.BRIGHT_TEXT_COLOR);
        usernameField.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        ((AbstractDocument) usernameField.getDocument()).setDocumentFilter(new TextLengthFilter(16));

        passwordField = new JPasswordField(20);
        passwordField.setBackground(MainMenuFrame.SIDE_COLOR);
        passwordField.setForeground(MainMenuFrame.BRIGHT_TEXT_COLOR);
        passwordField.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setForeground(MainMenuFrame.TEXT_COLOR);

        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setForeground(MainMenuFrame.TEXT_COLOR);

        newUserCheckbox = new JCheckBox("I am a new user!");
        newUserCheckbox.setBackground(MainMenuFrame.SIDE_COLOR);
        newUserCheckbox.setForeground(MainMenuFrame.TEXT_COLOR);

        submitButton = new JButton("Start DuberChatting!");
        submitButton.setBackground(MainMenuFrame.TEXT_COLOR);
        submitButton.setForeground(MainMenuFrame.MAIN_COLOR);
        submitButton.addActionListener(new SubmitActionListener());

        optionsButton = new JButton("⚙️");
        optionsButton.setBackground(MainMenuFrame.TEXT_COLOR);
        optionsButton.setForeground(MainMenuFrame.MAIN_COLOR);
        optionsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (settingsFrame != null) {
                    if (settingsFrame.isVisible()) {
                        return;
                    }

                    settingsFrame.destroy();
                }

                settingsFrame = new LoginSettingFrame(client);
                settingsFrame.setVisible(true);
            }
        });

        connectingText = new JLabel("Connecting...");
        connectingText.setForeground(MainMenuFrame.TEXT_COLOR);

        failedText = new JLabel("Login failed! Try again.");
        failedText.setForeground(Color.RED);
        // JLabel image = new JLabel(new ImageIcon("/data/server/duberchat.png"));
        // constraints.gridx = 0;
        // constraints.gridy = 0;
        // constraints.anchor = GridBagConstraints.CENTER;
        // mainPanel.add(image, constraints);
        // image.repaint();
        // mainPanel.repaint();

        addConstrainedComponent(usernameLabel, mainPanel, loginLayout, constraints, 0, 0, 1, 1,
                GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER, new Insets(0, 0, 0, 0));
        addConstrainedComponent(usernameField, mainPanel, loginLayout, constraints, 0, 1, 1, 1,
                GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER, new Insets(0, 0, 0, 0));
        addConstrainedComponent(passwordLabel, mainPanel, loginLayout, constraints, 0, 2, 1, 1,
                GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER, new Insets(30, 0, 0, 0));
        addConstrainedComponent(passwordField, mainPanel, loginLayout, constraints, 0, 3, 1, 1,
                GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER, new Insets(8, 0, 16, 0));
        addConstrainedComponent(newUserCheckbox, mainPanel, loginLayout, constraints, 0, 4, 1, 1,
                GridBagConstraints.NONE, GridBagConstraints.CENTER, new Insets(8, 0, 8, 0));
        addConstrainedComponent(submitButton, mainPanel, loginLayout, constraints, 0, 5, 1, 1,
                GridBagConstraints.REMAINDER, GridBagConstraints.CENTER, new Insets(8, 0, 8, 0));
        addConstrainedComponent(optionsButton, mainPanel, loginLayout, constraints, 0, 6, 1, 1, GridBagConstraints.NONE,
                GridBagConstraints.CENTER, new Insets(16, 0, 0, 0));

        this.add(mainPanel);
    }

    /**
     * Requests a reload for a frame.
     * <p>
     * This method should only be called as a result of a failed {@code AuthEvent},
     * as this method is responsible for initializing the visual prompt for the GUI,
     * signifying that a login/registration attempt has failed.
     */
    public void reload() {
        this.handleFailedRequest();
    }

    /**
     * Handles a failed auth request.
     * <p>
     * {@code AuthFailed} event should only be returned if the login was
     * unsuccessful (wrong username/password combo) or if the client was attempting
     * to create a new user with an existing username. More details can be found in
     * the {@link duberchat.events.AuthEvent AuthEvent} and
     * {@link duberchat.events.AuthFailedEvent AuthFailedEvent}.
     * <p>
     * This method will display a visual prompt for the user to alert them that an
     * auth request has failed.
     */
    private void handleFailedRequest() {
        if (connectingText != null) {
            mainPanel.remove(connectingText);
        }

        addConstrainedComponent(failedText, mainPanel, loginLayout, constraints, 0, 7, 1, 1, GridBagConstraints.NONE,
                GridBagConstraints.CENTER, new Insets(8, 0, 8, 0));

        this.revalidate();
        this.repaint();

        // Reset request sent status
        alreadySentRequest = false;
    }

    class SubmitActionListener implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            // Ensure that multiple login events aren't performed
            if (alreadySentRequest)
                return;

            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            passwordField.setText("");

            boolean isNewUser = newUserCheckbox.isSelected();

            LoginFrame.this.output.offer(new ClientLoginEvent(client.getUser(), isNewUser, username, password));

            // Add connecting... text to aid user
            mainPanel.remove(failedText); // attempt to remove failed text
            addConstrainedComponent(connectingText, mainPanel, loginLayout, constraints, 0, 7, 1, 1,
                    GridBagConstraints.NONE, GridBagConstraints.CENTER, new Insets(8, 0, 8, 0));
            mainPanel.revalidate();
            mainPanel.repaint();

            // Make sure user cannot bomb server with connection requests
            alreadySentRequest = true;

            System.out.println("SYSTEM: offered login request.");
        }
    }
}