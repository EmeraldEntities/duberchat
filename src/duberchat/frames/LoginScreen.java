package duberchat.frames;

import java.awt.event.*;
import javax.swing.*;
import java.awt.*;

import java.util.EventObject;
import java.util.concurrent.ConcurrentLinkedQueue;

import duberchat.events.*;
import duberchat.client.ChatClient;

@SuppressWarnings("serial")
public class LoginScreen extends ReloadableGridbagFrame {
    final Dimension SIZE = new Dimension(500, 600);

    JPanel mainPanel;
    JTextField usernameField;
    JPasswordField passwordField;
    JCheckBox newUserCheckbox;
    JButton submitButton;
    GridBagLayout loginLayout;
    GridBagConstraints constraints;

    ChatClient client;
    ConcurrentLinkedQueue<SerializableEvent> output;

    public LoginScreen(ChatClient client, ConcurrentLinkedQueue<SerializableEvent> outgoingEvents) {
        super("DuberChat");

        this.client = client;
        this.output = outgoingEvents;

        mainPanel = new JPanel();
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(this.SIZE);
        this.setResizable(false);
        this.setUndecorated(true);

        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width / 2 - this.getSize().width / 2, dim.height / 2 - this.getSize().height / 2);

        // =============================================
        loginLayout = new GridBagLayout();
        constraints = new GridBagConstraints();

        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);

        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setForeground(Color.WHITE);
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setForeground(Color.WHITE);

        newUserCheckbox = new JCheckBox("I am a new user!");
        submitButton = new JButton("Start DuberChatting!");
        submitButton.addActionListener(new SubmitActionListener());

        this.addConstrainedComponent(usernameLabel, mainPanel, loginLayout, constraints, 0, 0,
                GridBagConstraints.REMAINDER, 1);
        this.addConstrainedComponent(usernameField, mainPanel, loginLayout, constraints, 0, 20,
                GridBagConstraints.REMAINDER, 1);
        this.addConstrainedComponent(passwordLabel, mainPanel, loginLayout, constraints, 0, 60,
                GridBagConstraints.REMAINDER, 1);
        this.addConstrainedComponent(passwordField, mainPanel, loginLayout, constraints, 0, 80,
                GridBagConstraints.REMAINDER, 1);
        this.addConstrainedComponent(newUserCheckbox, mainPanel, loginLayout, constraints, 0, 120, 1, 1);
        this.addConstrainedComponent(submitButton, mainPanel, loginLayout, constraints, 0, 160,
                GridBagConstraints.REMAINDER, 1);
        // =============================================
        mainPanel.setLayout(loginLayout);
        mainPanel.setSize(this.getSize());
        mainPanel.setBackground(Color.DARK_GRAY);

        this.add(mainPanel);
    }

    public void reload() {
        this.handleFailedRequest();
    }

    private void handleFailedRequest() {
        JLabel failedText = new JLabel("Login failed! Try again.");
        failedText.setForeground(Color.RED);
        this.addConstrainedComponent(failedText, mainPanel, loginLayout, constraints, 0, 200,
                GridBagConstraints.REMAINDER, 1);

        this.repaint();
    }

    class SubmitActionListener implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            usernameField.setText("");
            passwordField.setText("");

            boolean isNewUser = newUserCheckbox.isSelected();

            LoginScreen.this.output.offer(new ClientLoginEvent(client.getUser(), isNewUser, username, password));
            System.out.println("SYSTEM: offered login request.");
        }
    }
}