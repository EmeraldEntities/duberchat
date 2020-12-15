package duberchat.gui.frames;

import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JFrame;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Color;
import java.awt.Image;
import java.awt.Insets;

import duberchat.client.ChatClient;
import duberchat.gui.filters.TextLengthFilter;
import duberchat.gui.util.ComponentFactory;
import duberchat.events.ClientStatusUpdateEvent;
import duberchat.events.ClientPfpUpdateEvent;
import duberchat.events.ClientProfileUpdateEvent;
import duberchat.events.ClientPasswordUpdateEvent;
import duberchat.events.SerializableEvent;
import duberchat.chatutil.User;

/**
 * The {@code ProfileFrame} is the frame responsible for representing the
 * client's current user's information, and offers a place to edit user
 * information.
 * <p>
 * Upon a changed component, it is locally saved within this frame, but the
 * changes will not be saved on the server and sent to other users until the
 * save button is hit.
 * <p>
 * Created <b> 2020-12-11 </b>
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Joseph Wang
 */
@SuppressWarnings("serial")
public class ProfileFrame extends DynamicGridbagFrame {
    /** The default size for this frame. */
    public static final Dimension DEFAULT_SIZE = new Dimension(400, 500);
    /** The status choices available for the client to choose. */
    public static final String[] STATUS_CHOICES = { User.findStringStatus(User.ONLINE),
            User.findStringStatus(User.AWAY), User.findStringStatus(User.DND) };

    /** The associated client. */
    protected ChatClient client;
    /** The associated client's user. */
    private User user;

    /** The main panel of this frame. */
    private JPanel mainPanel;

    /** The user's profile picture label. */
    private JLabel profilePicture;
    /** The label with the user's username. */
    private JLabel usernameLabel;
    /** The save button, which saves all changes to this user. */
    private JButton saveButton;
    /** The label that describes the password field. */
    private JLabel passwordLabel;
    /** The field to enter the user's new password. */
    private JPasswordField passwordField;
    /** The label that describes the status combo box. */
    private JLabel statusUpdateLabel;
    /** The combo box with all the user's selectable statuses */
    private JComboBox<String> statusUpdateBox;
    /** The success text to be displayed on a successful profile change. */
    private JLabel succeedText;

    /** This user's current local profile picture. */
    private BufferedImage currentPfp;
    /** This user's current local profile picture's format. */
    private String currentPfpFormat;
    /** The icon with this user's local current profile pictures. */
    private ImageIcon currentPfpIcon;
    /** The icon with the "add Pfp" icon. */
    private ImageIcon addPfpIcon;
    /** This user's current local status. */
    private int status;

    /** The GridBagLayout for the main panel. */
    private GridBagLayout layout;
    /** The constraints for the layout. */
    private GridBagConstraints constraints;

    /**
     * Constructs a new {@code ProfileFrame} for the client's current user.
     * 
     * @param client the associated user.
     */
    public ProfileFrame(ChatClient client) {
        super(client.getUser().getUsername());

        this.user = client.getUser();
        this.client = client;
        this.status = user.getStatus();
        this.currentPfpFormat = user.getPfpFormat();

        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setSize(DEFAULT_SIZE);
        this.setResizable(false);
        this.setIconImage(new ImageIcon("data/system/logo.png").getImage());

        layout = new GridBagLayout();
        constraints = new GridBagConstraints();

        this.initializeComponents();
        this.addComponents();

        this.add(mainPanel);
    }

    /**
     * Initializes all the required components for this frame.
     */
    private void initializeComponents() {
        mainPanel = new JPanel();
        mainPanel.setSize(this.getSize());
        mainPanel.setBackground(MainFrame.PANEL_COLOR);
        mainPanel.setLayout(layout);

        try {
            this.currentPfp = user.getPfp();
            this.profilePicture = ComponentFactory
                    .createImageLabel(currentPfp.getScaledInstance(128, 128, Image.SCALE_SMOOTH));

            this.currentPfpIcon = new ImageIcon(currentPfp.getScaledInstance(128, 128, Image.SCALE_SMOOTH));
            this.addPfpIcon = new ImageIcon(ImageIO.read(new File("data/system/plus sign.png")).getScaledInstance(128,
                    128,
            Image.SCALE_SMOOTH));

            profilePicture.setIcon(currentPfpIcon);
            profilePicture.setBorder(BorderFactory.createRaisedBevelBorder());

        } catch (IOException e) {
            System.out.println("SYSTEM: Failed to load profile picture.");
        }

        // If user clicks the profile picture they can change it
        profilePicture.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                changeProfilePicture();
            }

            public void mouseEntered(MouseEvent e) {
                profilePicture.setIcon(addPfpIcon);
                reload();
            }

            public void mouseExited(MouseEvent e) {
                profilePicture.setIcon(currentPfpIcon);
                reload();
            }
        });

        String usernameText = user.getUsername() + " ("+ User.findStringStatus(status) + ")";
        usernameLabel = ComponentFactory.createLabel(usernameText, MainFrame.BRIGHT_TEXT_COLOR);
        saveButton = ComponentFactory.createButton("SAVE", MainFrame.MAIN_COLOR, MainFrame.TEXT_COLOR,
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        offerProfileChangeEvents();
                    }
                });
        // User can also change password
        passwordLabel = ComponentFactory.createLabel("Change Password", MainFrame.TEXT_COLOR);
        passwordField = ComponentFactory.createPasswordBox(20, MainFrame.BRIGHT_TEXT_COLOR, MainFrame.SIDE_COLOR,
                new TextLengthFilter(40), BorderFactory.createEmptyBorder(5, 5, 5, 5));
        passwordField.setText("");

        // User can also change their status to a selected array
        statusUpdateLabel = ComponentFactory.createLabel("Update Status", MainFrame.TEXT_COLOR);
        // Since offline is 0 and is not an option, we offset all statuses by 1 to match
        // with our selecteable list
        statusUpdateBox = ComponentFactory.createComboBox(STATUS_CHOICES, status - 1, MainFrame.BRIGHT_TEXT_COLOR,
                MainFrame.SIDE_COLOR, new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        updateStatus();
                    }
                });
        succeedText = ComponentFactory.createLabel("Saved!", Color.CYAN);
    }

    /**
     * Adds all required components onto the main panel.
     */
    private void addComponents() {
        addConstrainedComponent(profilePicture, mainPanel, layout, constraints, 0, 0, 1, 1,
                GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER, new Insets(20, 20, 20, 20));
        addConstrainedComponent(usernameLabel, mainPanel, layout, constraints, 0, 1, 1, 1,
                GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER, new Insets(0, 0, 40, 0));
        
        addConstrainedComponent(statusUpdateLabel, mainPanel, layout, constraints, 0, 2, 1, 1,
                GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER, new Insets(0, 0, 0, 0));
        addConstrainedComponent(statusUpdateBox, mainPanel, layout, constraints, 0, 3, 1, 1,
                GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER, new Insets(8, 0, 16, 0));

        addConstrainedComponent(passwordLabel, mainPanel, layout, constraints, 0, 4, 1, 1,
                GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER, new Insets(0, 0, 0, 0));
        addConstrainedComponent(passwordField, mainPanel, layout, constraints, 0, 5, 1, 1,
                GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER, new Insets(8, 0, 16, 0));

        addConstrainedComponent(saveButton, mainPanel, layout, constraints, 0, 6, 1, 1, GridBagConstraints.REMAINDER,
                GridBagConstraints.CENTER, new Insets(8, 0, 8, 0));
    }

    /**
     * {@inheritDoc}
     * <p>
     * Reloads all the components and ensures new updates occur.
     */
    public void reload() {
        this.repaint();
        this.revalidate();
    }

    /**
     * {@inheritDoc}
     * <p>
     * If this component receives a ClientProfileUpdateEvent that originated from
     * our client user, we know that our request succeeded.
     */
    public void reload(SerializableEvent event) {
        if (event instanceof ClientProfileUpdateEvent) {
            addConstrainedComponent(succeedText, mainPanel, layout, constraints, 0, 7, 1, 1,
                    GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER, new Insets(0, 0, 0, 0));
        }

        this.reload();
    }

    /**
     * Attempts to change this user's profile picture, but does not send an event to
     * the server.
     * <p>
     * This method only locally saves the image. The save button must be pressed to
     * send the event.
     */
    private void changeProfilePicture() {
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setFileFilter(new FileNameExtensionFilter("Image Files", ImageIO.getReaderFileSuffixes()));
        fc.setAcceptAllFileFilterUsed(false);
        int option = fc.showDialog(ProfileFrame.this, "Upload");

        if (option == JFileChooser.APPROVE_OPTION) {
            try {
                String fileLocation = fc.getSelectedFile().toString();
                // Get extension of the file
                this.currentPfpFormat = fileLocation.substring(fileLocation.lastIndexOf(".") + 1);

                this.currentPfp = ImageIO.read(fc.getSelectedFile().getAbsoluteFile());
                this.currentPfpIcon = new ImageIcon(currentPfp.getScaledInstance(128, 128, Image.SCALE_SMOOTH));
                profilePicture.setIcon(currentPfpIcon);
            } catch (IOException e2) {
                System.out.println("SYSTEM: Failed to load new profile picture");
            }

            this.reload();
        }
    }

    /**
     * Attempts to update this user's status, but does not send an event to the
     * server.
     * <p>
     * This method only locally saves the status. The save button must be pressed to
     * send the event.
     * 
     * @param user
     */
    private void updateStatus() {
        int statusConstant = User.findIntStatus((String) statusUpdateBox.getSelectedItem());
        if (statusConstant != -1) {
            status = statusConstant;

            String newUsernameText = user.getUsername() + " (" + User.findStringStatus(status) + ")";
            usernameLabel.setText(newUsernameText);
            reload();
        }
    }

    /**
     * Offers the profile change events with the changed profile component to the
     * outgoing events queue.
     * <p>
     * Sends events to the server. The events sent are dependant on what components
     * are changed. If a component is unchanged, an event to change that component
     * will not be sent.
     */
    private void offerProfileChangeEvents() {
        String clientUsername = this.client.getUser().getUsername();

        if (client.getUser().getStatus() != status) {
            client.offerEvent(new ClientStatusUpdateEvent(clientUsername, status));
        }

        if (!client.getUser().pfpEquals(currentPfp)) {
            client.offerEvent(new ClientPfpUpdateEvent(clientUsername, currentPfp, currentPfpFormat));
        }

        String passwordString = new String(passwordField.getPassword());
        if (!passwordString.equals("")) {
            long newHashedPassword = passwordString.hashCode();
            passwordField.setText("");
            client.offerEvent(new ClientPasswordUpdateEvent(clientUsername, newHashedPassword));
        }
    }
}
