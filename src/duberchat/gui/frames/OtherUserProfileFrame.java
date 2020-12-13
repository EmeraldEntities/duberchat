package duberchat.gui.frames;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;

import duberchat.events.ChannelCreateEvent;
import duberchat.events.ChannelRemoveMemberEvent;
import duberchat.events.ChannelPromoteMemberEvent;
import duberchat.events.ChannelDemoteMemberEvent;
import duberchat.events.FriendAddEvent;
import duberchat.client.ChatClient;
import duberchat.gui.util.ComponentFactory;
import duberchat.chatutil.Channel;
import duberchat.chatutil.User;

/**
 * This class is used primarily as an information/action frame for when a user is clicked on the
 * side panel, and is designed to provide easy access of information for that user.
 * <p>
 * This frame will destroy itself upon loss of focus or upon an action, for ease of synchronization and convenience to the client.
 * <p>
 * Created <b>2020-12-10</b>
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Joseph Wang
 */
@SuppressWarnings("serial")
public class OtherUserProfileFrame extends DynamicGridbagFrame {
    /** The default size of this frame. */
    public static final Dimension DEFAULT_SIZE = new Dimension(300, 400);

    /** The associated client. */
    protected ChatClient client;
    /** The user that this frame is representing. */
    private User otherUser;

    /** The main panel for this frame. */
    private JPanel mainPanel;

    /** The profile picture label. */
    private JLabel profilePicture;
    /** The username label. */
    private JLabel usernameLabel;
    /** The dm button that starts a chat with this user. */
    private JButton dmButton;
    /** The add friend button that adds this user as a friend. */
    private JButton addFriendButton;
    /** The remove button that removes this user from the channel. */
    private JButton removeButton;
    /** The promote button that OPs this user. */
    private JButton promoteButton;
    /** The demote button that de-OPs this user. */
    private JButton demoteButton;

    /** The GridBagLayout for this frame. */
    private GridBagLayout layout;
    /** A shared constraints object for working with the layout. */
    private GridBagConstraints constraints;

    /** True if the client is an admin. */
    private boolean isClientAdmin;
    /** True if the client is already friends with this user. */
    private boolean isAlreadyFriends;

    /**
     * Constructs a new {@code OtherUserProfileFrame}.
     * 
     * @param client        the associated client.
     * @param otherUser     the user this frame is representing.
     * @param clickLocation the starting click location of the mouse that
     *                      initialized the creation of this frame.
     * @param isAdmin       whether this user is an admin or not.
     */
    public OtherUserProfileFrame(ChatClient client, User otherUser, Point clickLocation, boolean isAdmin) {
        super(otherUser.getUsername());

        this.client = client;
        this.otherUser = otherUser;
        this.isClientAdmin = client.getCurrentChannel().getAdminUsers().contains(client.getUser());
        this.isAlreadyFriends = client.getFriends().containsKey(otherUser.getUsername());

        // Initialize this JFrame
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setSize(DEFAULT_SIZE);
        this.setResizable(false);
        this.setUndecorated(true);
        this.setIconImage(new ImageIcon("data/system/logo.png").getImage());
        this.setLocation(clickLocation.x - this.getWidth(), clickLocation.y);
        // Close this frame upon focus lost
        this.addWindowListener(new WindowAdapter() {
            public void windowDeactivated(WindowEvent e) {
                reload();
            }
        });

        this.initializeComponents();

        // Add all components
        addConstrainedComponent(profilePicture, mainPanel, layout, constraints, 0, 0, 1, 1,
                GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER, new Insets(20, 20, 20, 20));
        addConstrainedComponent(usernameLabel, mainPanel, layout, constraints, 0, 1, 1, 1,
                GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER, new Insets(0, 0, 40, 0));
        if (!isAlreadyFriends && !otherUser.equals(this.client.getUser())) {
            addConstrainedComponent(addFriendButton, mainPanel, layout, constraints, 0, 2, 1, 1,
                    GridBagConstraints.REMAINDER, GridBagConstraints.CENTER, new Insets(0, 0, 8, 0));
        }
        addConstrainedComponent(dmButton, mainPanel, layout, constraints, 0, 3, 1, 1, GridBagConstraints.REMAINDER,
                GridBagConstraints.CENTER, new Insets(0, 0, 16, 0));

        // Only specfically add these buttons if client is admin and this user is not
        // client
        if (isClientAdmin && !otherUser.equals(this.client.getUser())) {
            addConstrainedComponent(removeButton, mainPanel, layout, constraints, 0, 4, 1, 1,
                    GridBagConstraints.REMAINDER, GridBagConstraints.CENTER, new Insets(0, 0, 8, 0));

            // If this user is already admin, show demote button
            // Otherwise show promote button
            if (isAdmin) {
                addConstrainedComponent(demoteButton, mainPanel, layout, constraints, 0, 5, 1, 1,
                        GridBagConstraints.REMAINDER, GridBagConstraints.CENTER, new Insets(0, 0, 0, 0));
            } else {
                addConstrainedComponent(promoteButton, mainPanel, layout, constraints, 0, 5, 1, 1,
                        GridBagConstraints.REMAINDER, GridBagConstraints.CENTER, new Insets(0, 0, 0, 0));
            }
        }

        this.add(mainPanel);
    }

    /**
     * Initializes all the components needed for this frame.
     * <p>
     * Certain buttons will not be loaded depending on the permission level of the
     * client and this user, etc.
     */
    private void initializeComponents() {
        layout = new GridBagLayout();
        constraints = new GridBagConstraints();

        // Initialize main panel
        mainPanel = new JPanel();
        mainPanel.setSize(this.getSize());
        mainPanel.setBackground(MainFrame.PANEL_COLOR);
        mainPanel.setLayout(layout);

        profilePicture = ComponentFactory
        .createImageLabel(otherUser.getPfp().getScaledInstance(128, 128, Image.SCALE_SMOOTH));
        profilePicture.setBorder(BorderFactory.createRaisedBevelBorder());

        String usernameText = otherUser.getUsername() + " ("+ otherUser.getStringStatus() + ")";
        usernameLabel = ComponentFactory.createLabel(usernameText, MainFrame.BRIGHT_TEXT_COLOR);
        dmButton = ComponentFactory.createButton("Duberchat!", MainFrame.PANEL_COLOR, MainFrame.TEXT_COLOR,
                new CreateChannelActionListener());
                
        // Don't load these components unless we need to
        // (if client is admin and they aren't this user)
        if (!otherUser.equals(this.client.getUser())) {
            addFriendButton = ComponentFactory.createButton("Add Friend", MainFrame.PANEL_COLOR, MainFrame.TEXT_COLOR,
                    new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            addFriend();
                        }
                    });
        }

        if (isClientAdmin) {
            removeButton = ComponentFactory.createButton("Remove User", MainFrame.PANEL_COLOR, MainFrame.TEXT_COLOR,
                    new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            if (isClientAdmin) {
                                removeUser();
                            }

                        }
                    });
            promoteButton = ComponentFactory.createButton("Promote user (OP)", MainFrame.PANEL_COLOR,
                    MainFrame.TEXT_COLOR, new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            if (isClientAdmin) {
                                promoteUser();
                            }
                        }
                    });
            demoteButton = ComponentFactory.createButton("Demote user (De-OP)", MainFrame.PANEL_COLOR,
                    MainFrame.TEXT_COLOR, new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            if (isClientAdmin) {
                                demoteUser();
                            }
                        }
                    });
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Reloading this frame destroys this frame.
     */
    public void reload() {
        this.destroy();
    }

    /**
     * Promotes the user this frame is representing and sends a promotion event.
     */
    private void promoteUser() {
        // client current channel should be this user panel's channel
        User clientUser = new User(client.getUser());
        Channel curChannel = new Channel(client.getCurrentChannel());
        client.offerEvent(
                new ChannelPromoteMemberEvent(clientUser, curChannel, otherUser.getUsername()));

        this.destroy();
    }

    /**
     * Demotes the user this frame is representing and sends a demotion event.
     */
    private void demoteUser() {
        User clientUser = new User(client.getUser());
        Channel curChannel = new Channel(client.getCurrentChannel());
        client.offerEvent(
                new ChannelDemoteMemberEvent(clientUser, curChannel, otherUser.getUsername()));

        this.destroy();
    }

    /**
     * Removes the user this frame is representing and sends a removal event.
     */
    private void removeUser() {
        User clientUser = new User(client.getUser());
        Channel curChannel = new Channel(client.getCurrentChannel());
        client.offerEvent(new ChannelRemoveMemberEvent(clientUser, curChannel, otherUser.getUsername()));

        this.destroy();
    }

    /**
     * Adds this user to the client's friend page.
     */
    private void addFriend() {
        User clientUser = new User(client.getUser());
        client.offerEvent(new FriendAddEvent(clientUser, otherUser.getUsername()));

        this.destroy();
    }

    /**
     * The {@code CreateChannelActionListener} is designed to create a new channel
     * when its {@link #actionPerformed(ActionEvent)} method is invoked, and handle
     * all the logic behind creating a new channel.
     * <p>
     * Created <b>2020-12-10</b>
     * 
     * @since 1.0.0
     * @version 1.0.0
     * @author Joseph Wang
     */
    private class CreateChannelActionListener implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            // We can't make a channel with ourselves this way
            if (otherUser.equals(client.getUser())) {
                return;
            }

            Channel newChannel = new Channel("@" + otherUser.getUsername());
            User clientUser = new User(client.getUser());
            client.offerEvent(new ChannelCreateEvent(clientUser, newChannel, otherUser.getUsername()));

            destroy();
        }
    }
}
