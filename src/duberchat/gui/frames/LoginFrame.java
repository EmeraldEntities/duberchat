package duberchat.gui.frames;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Toolkit;
import java.awt.Color;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JCheckBox;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPasswordField;
import javax.swing.JLabel;

import duberchat.events.ClientLoginEvent;
import duberchat.client.ChatClient;
import duberchat.gui.filters.TextLengthFilter;
import duberchat.gui.filters.LimitingRegexFilter;
import duberchat.gui.util.ComponentFactory;

/**
 * This class is designed to construct the login page, which is the first frame
 * that users who start up this application will see.
 * <p>
 * The login frame is intended to be non-moveable and in the middle of the first
 * monitor, simulating login frames of other popular chatting services like
 * Discord.
 * <p>
 * Created <b> 2020-12-09 </b>
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Joseph Wang
 */
@SuppressWarnings("serial")
public class LoginFrame extends DynamicGridbagFrame {
    /** The default size of this frame. */
    public static final Dimension DEFAULT_SIZE = new Dimension(400, 500);

    /** The main panel of this frame. */
    private JPanel mainPanel;

    /** The field to enter a username. */
    private JTextField usernameField;
    /** The field to enter a password. */
    private JPasswordField passwordField;
    /** The checkbox that indicates whether or not this user is a new user. */
    private JCheckBox newUserCheckbox;
    /** The submit button, which attempts to offer a login request to the server. */
    private JButton submitButton;
    /** The options button for opening up the options pane. */
    private JButton optionsButton;
    /** The label for displaying "Username" for the username field. */
    private JLabel usernameLabel;
    /** The label for displaying "Password" for the password field. */
    private JLabel passwordLabel;
    /** The duberchat logo label. */
    private JLabel picLabel;

    /** The GridBagLayout for this frame. */
    private GridBagLayout loginLayout;
    /** A shared constraints object for working with the layout. */
    private GridBagConstraints constraints;

    /** The text that indicates a connection is being made. */
    private JLabel connectingText;
    /** The failed text that appears upon an auth failed event. */
    private JLabel failedText;

    /** The associated client. */
    protected ChatClient client;
    /** The child settings frame that spawned from this frame. */
    private LoginSettingFrame settingsFrame;

    /** If this frame has already sent a login request and is waiting for auth. */
    private boolean alreadySentRequest = false;

    /**
     * Constructs a new {@code LoginFrame}.
     * <p>
     * Initializes all this frame's properties and all the components, and adds them
     * to this frame.
     * 
     * @param client the associated client.
     */
    public LoginFrame(ChatClient client) {
        super("DuberChat");

        this.client = client;

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

        // Set window to middle of screen
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width / 2 - this.getSize().width / 2, dim.height / 2 - this.getSize().height / 2);

        this.initializeComponents();
        this.addComponents();

        this.getRootPane().setDefaultButton(submitButton);
        this.add(mainPanel);
    }

    /**
     * Initializes all the required components for this frame.
     */
    private void initializeComponents() {
        usernameField = ComponentFactory.createTextBox(20, MainFrame.BRIGHT_TEXT_COLOR, MainFrame.SIDE_COLOR,
                new LimitingRegexFilter(16, "[\\w]{1}[\\w\\.-]*"), BorderFactory.createEmptyBorder(5, 5, 5, 5));
        passwordField = ComponentFactory.createPasswordBox(20, MainFrame.BRIGHT_TEXT_COLOR, MainFrame.SIDE_COLOR,
                new TextLengthFilter(40), BorderFactory.createEmptyBorder(5, 5, 5, 5));

        usernameLabel = ComponentFactory.createLabel("Username", MainFrame.TEXT_COLOR);
        passwordLabel = ComponentFactory.createLabel("Password", MainFrame.TEXT_COLOR);
        connectingText = ComponentFactory.createLabel("Connecting...", MainFrame.TEXT_COLOR);
        failedText = ComponentFactory.createLabel("Login failed! Try again.", Color.RED);

        newUserCheckbox = ComponentFactory.createCheckbox("I am a new user", MainFrame.TEXT_COLOR,
                MainFrame.SIDE_COLOR);

        submitButton = ComponentFactory.createButton("Start DuberChatting", MainFrame.MAIN_COLOR, MainFrame.TEXT_COLOR);
        submitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                attemptLogin();
            }
        });

        optionsButton = ComponentFactory.createImageButton("", "data/system/settings.png", 16, 16, MainFrame.MAIN_COLOR,
                MainFrame.TEXT_COLOR);
        optionsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                initializeSettingsPane();
            }
        });

        picLabel = ComponentFactory.createImageLabel("", "data/system/duberchat.png", 128, -1, MainFrame.TEXT_COLOR);
    }

    /**
     * Adds all the required components to the main panel of this frame.
     */
    private void addComponents() {
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
     * {@inheritDoc}
     * <p>
     * Also destroys any child frames (frames that spawned from this frame).
     */
    public void destroy() {
        if (this.hasActiveSettingsFrame()) {
            this.settingsFrame.destroy();
        }

        super.destroy();
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

        if (newUserCheckbox.isSelected()) {
            failedText.setText("Failed! User already exists.");
        } else {
            failedText.setText("Failed! Wrong username/password.");
        }
        addConstrainedComponent(failedText, mainPanel, loginLayout, constraints, 0, 8, 1, 1, GridBagConstraints.NONE,
                GridBagConstraints.CENTER, new Insets(8, 0, 8, 0));

        this.revalidate();
        this.repaint();

        // Reset request sent status
        alreadySentRequest = false;
    }

    /**
     * Attempts to send a login request to the server, using the information
     * provided in the fields and the checkbox.
     * <p>
     * This method will only actually send a request if it has not already and if
     * neither fields are left blank.
     * <p>
     * This method will clear the password field and will also mount a load text
     * onto the panel, signifying to the user that a connection is being made.
     */
    private void attemptLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        // Ensure that multiple login events aren't performed and that request are valid
        if (alreadySentRequest || password.equals("") || username.equals("")) {
            return;
        }

        passwordField.setText("");

        boolean isNewUser = newUserCheckbox.isSelected();

        client.offerEvent(new ClientLoginEvent(client.getUser(), isNewUser, username, password));

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

    /**
     * Attempts to initialize the login settings pane, if necessary.
     */
    private void initializeSettingsPane() {
        if (settingsFrame != null) {
            if (settingsFrame.isVisible()) {
                return;
            }
            settingsFrame.destroy();
        }

        settingsFrame = new LoginSettingFrame(client);
        settingsFrame.setVisible(true);
    }

    /**
     * Checks if this login frame has an active settings frame.
     * 
     * @return true if this login frame has an active settings frame.
     */
    public boolean hasActiveSettingsFrame() {
        return (settingsFrame != null && settingsFrame.isVisible());
    }

    /**
     * Retrieves this login frame's setting frame.
     * 
     * @return this login frame's setting frame.
     */
    public LoginSettingFrame getSettingsFrame() {
        return this.settingsFrame;
    }
}