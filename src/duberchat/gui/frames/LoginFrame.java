package duberchat.gui.frames;

import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Toolkit;
import java.awt.Color;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseAdapter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.imageio.ImageIO;

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
import duberchat.chatutil.User;
import duberchat.client.ChatClient;
import duberchat.gui.filters.TextLengthFilter;
import duberchat.gui.filters.LimitingRegexFilter;
import duberchat.gui.util.ComponentFactory;

/**
 * This class is designed to construct the login page, which is the first frame
 * that users who start up this application will see.
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

    JPanel mainPanel;

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

    boolean alreadySentRequest = false;

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

        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width / 2 - this.getSize().width / 2, dim.height / 2 - this.getSize().height / 2);
        
        initializeComponents(client);

        this.add(mainPanel);
    }

    private void initializeComponents(ChatClient client) {
        usernameField = ComponentFactory.createTextBox(20, MainFrame.BRIGHT_TEXT_COLOR, MainFrame.SIDE_COLOR,
                new LimitingRegexFilter(16, "[\\w]{1}[\\w\\.-]*"), BorderFactory.createEmptyBorder(5, 5, 5, 5));
        passwordField = ComponentFactory.createPasswordBox(20, MainFrame.BRIGHT_TEXT_COLOR, MainFrame.SIDE_COLOR,
                new TextLengthFilter(40), BorderFactory.createEmptyBorder(5, 5, 5, 5));

        passwordField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    attemptSendingLoginRequest();
                }
            }
        });

        JLabel usernameLabel = ComponentFactory.createLabel("Username", MainFrame.TEXT_COLOR);
        JLabel passwordLabel = ComponentFactory.createLabel("Password", MainFrame.TEXT_COLOR);
        connectingText = ComponentFactory.createLabel("Connecting...", MainFrame.TEXT_COLOR);
        failedText = ComponentFactory.createLabel("Login failed! Try again.", Color.RED);

        newUserCheckbox = ComponentFactory.createCheckbox("I am a new user", MainFrame.TEXT_COLOR,
                MainFrame.SIDE_COLOR);

        submitButton = ComponentFactory.createButton("Start DuberChatting", MainFrame.MAIN_COLOR, MainFrame.TEXT_COLOR,
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        attemptSendingLoginRequest();
                    }
                });

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

        JLabel picLabel = ComponentFactory.createImageLabel("", "data/system/duberchat.png", 128, -1,
                MainFrame.TEXT_COLOR);


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

    private void attemptSendingLoginRequest() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        // Ensure that multiple login events aren't performed
        if (alreadySentRequest || password.equals("") || username.equals("")) {
            return;
        }

        passwordField.setText("");

        boolean isNewUser = newUserCheckbox.isSelected();
        
        User clientUser = new User(client.getUser());
        client.offerEvent(new ClientLoginEvent(clientUser, isNewUser, username, password));

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