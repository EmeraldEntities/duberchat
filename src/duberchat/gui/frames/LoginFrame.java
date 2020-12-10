package duberchat.gui.frames;

import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;

import java.util.concurrent.ConcurrentLinkedQueue;

import duberchat.events.*;
import duberchat.client.ChatClient;
import duberchat.gui.filters.TextLengthFilter;
import duberchat.gui.util.ComponentFactory;
import duberchat.gui.util.FrameFactory;

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
        this.setIconImage(new ImageIcon("data/system/logo.png").getImage());

        loginLayout = new GridBagLayout();
        constraints = new GridBagConstraints();

        mainPanel = new JPanel();
        mainPanel.setSize(this.getSize());
        mainPanel.setBackground(MainFrame.MAIN_COLOR);
        mainPanel.setLayout(loginLayout);

        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width / 2 - this.getSize().width / 2, dim.height / 2 - this.getSize().height / 2);

        // =============================================

        usernameField = ComponentFactory.createTextBox(20, MainFrame.BRIGHT_TEXT_COLOR, MainFrame.SIDE_COLOR,
                new TextLengthFilter(16), BorderFactory.createEmptyBorder(5, 5, 5, 5));
        passwordField = ComponentFactory.createPasswordBox(20, MainFrame.BRIGHT_TEXT_COLOR, MainFrame.SIDE_COLOR,
                new TextLengthFilter(40), BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JLabel usernameLabel = ComponentFactory.createLabel("Username", MainFrame.TEXT_COLOR);
        JLabel passwordLabel = ComponentFactory.createLabel("Password", MainFrame.TEXT_COLOR);
        connectingText = ComponentFactory.createLabel("Connecting...", MainFrame.TEXT_COLOR);
        failedText = ComponentFactory.createLabel("Login failed! Try again.", Color.RED);

        newUserCheckbox = ComponentFactory.createCheckbox("I am a new user", MainFrame.TEXT_COLOR,
                MainFrame.SIDE_COLOR);

        submitButton = ComponentFactory.createButton("Start DuberChatting", MainFrame.MAIN_COLOR, MainFrame.TEXT_COLOR,
                new SubmitActionListener());

        ImageIcon settingsIcon = new ImageIcon();
        try {
            BufferedImage settings = ImageIO.read(new File("data/system/settings.png"));
            settingsIcon = new ImageIcon(settings.getScaledInstance(16, 16, Image.SCALE_SMOOTH));
        } catch (IOException e) {
            System.out.println("SYSTEM: Could not load gear!");
        }
        optionsButton = ComponentFactory.createButton("", MainFrame.MAIN_COLOR, MainFrame.TEXT_COLOR);
        optionsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // JOptionPane pain = ComponentFactory.createRequestPane();
                // String text = JOptionPane.showInputDialog(pain, "please help me");
                // JDialog helpme = pain.createDialog(pain, "PAIIIN");
                // helpme.setVisible(true);

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
        optionsButton.setIcon(settingsIcon);

        JLabel picLabel = new JLabel("");
        try {
            BufferedImage logo = ImageIO.read(new File("data/system/duberchat.png"));
            picLabel = new JLabel(new ImageIcon(logo.getScaledInstance(128, -1, Image.SCALE_SMOOTH)));
        } catch (IOException e) {
            System.out.println("SYSTEM: Could not load logo!");
        }

        // JLabel image = new JLabel(new ImageIcon("/data/server/duberchat.png"));
        // constraints.gridx = 0;
        // constraints.gridy = 0;
        // constraints.anchor = GridBagConstraints.CENTER;
        // mainPanel.add(image, constraints);
        // image.repaint();
        // mainPanel.repaint();

        addConstrainedComponent(picLabel, mainPanel, loginLayout, constraints, 0, 0, 1, 1,
                GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER, new Insets(0, 0, 20, 0));
        addConstrainedComponent(usernameLabel, mainPanel, loginLayout, constraints, 0, 1, 1, 1,
                GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER, new Insets(0, 0, 0, 0));
        addConstrainedComponent(usernameField, mainPanel, loginLayout, constraints, 0, 2, 1, 1,
                GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER, new Insets(8, 0, 0, 0));
        addConstrainedComponent(passwordLabel, mainPanel, loginLayout, constraints, 0, 3, 1, 1,
                GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER, new Insets(30, 0, 0, 0));
        addConstrainedComponent(passwordField, mainPanel, loginLayout, constraints, 0, 4, 1, 1,
                GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER, new Insets(8, 0, 16, 0));
        addConstrainedComponent(newUserCheckbox, mainPanel, loginLayout, constraints, 0, 5, 1, 1,
                GridBagConstraints.NONE, GridBagConstraints.CENTER, new Insets(8, 0, 8, 0));
        addConstrainedComponent(submitButton, mainPanel, loginLayout, constraints, 0, 6, 1, 1,
                GridBagConstraints.REMAINDER, GridBagConstraints.CENTER, new Insets(8, 0, 8, 0));
        addConstrainedComponent(optionsButton, mainPanel, loginLayout, constraints, 0, 7, 1, 1, GridBagConstraints.NONE,
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

        addConstrainedComponent(failedText, mainPanel, loginLayout, constraints, 0, 8, 1, 1, GridBagConstraints.NONE,
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
            addConstrainedComponent(connectingText, mainPanel, loginLayout, constraints, 0, 8, 1, 1,
                    GridBagConstraints.NONE, GridBagConstraints.CENTER, new Insets(8, 0, 8, 0));
            mainPanel.revalidate();
            mainPanel.repaint();

            // Make sure user cannot bomb server with connection requests
            alreadySentRequest = true;

            System.out.println("SYSTEM: offered login request.");
        }
    }
}