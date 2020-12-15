package duberchat.gui.panels;

import javax.swing.JPanel;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;

import duberchat.chatutil.User;
import duberchat.client.ChatClient;
import duberchat.events.ChannelCreateEvent;
import duberchat.events.FriendRemoveEvent;
import duberchat.gui.frames.DynamicGridbagFrame;
import duberchat.gui.frames.MainFrame;
import duberchat.gui.util.ComponentFactory;

/**
 * The {@code FriendPanel} is designed to act like a responsive way to display
 * and interact with friends.
 * <p>
 * Friends should only show if the current channel is null.
 * <p>
 * This panel is designed to be able to be constructed fast and simply. It
 * optimizes what it can.
 * <p>
 * Created <b>2020-12-12</b>
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Joseph Wang
 */
@SuppressWarnings("serial")
public class FriendPanel extends JPanel {
    /** The header font. */
    private static final Font HEADER_FONT = new Font("courier", Font.BOLD, 12);
    /** The associated client. */
    protected ChatClient client;
    /** The friend to be represented by this panel. */
    private User friend;

    /** The chat button which starts a channel with the friend. */
    private JButton chatButton;
    /** The delete button that removes the friend from the client's friend list. */
    private JButton deleteButton;

    /** The friend's username and status. */
    private JLabel userLabel;
    /** The friend's profile picture. */
    private JLabel picture;

    /** The panel that holds the buttons. */
    private JPanel buttonPanel;

    /** The layout for the buttons. */
    private FlowLayout buttonLayout;
    /** The GridBagLayout for this frame. */
    private GridBagLayout layout;
    /** A shared constraints object for working with the layout. */
    private GridBagConstraints constraints;

    /**
     * Constructs a new {@code FriendPanel}. his panel is expected to be constructed
     * multiple times, and keeps that in mind with implementation.
     * 
     * @param client     the associated client.
     * @param friend     the friend to represent.
     * @param frameWidth the width of the parent frame.
     */
    public FriendPanel(ChatClient client, User friend, int frameWidth) {
        super();

        this.client = client;
        this.friend = friend;

        this.initializeComponents(frameWidth);
        this.reload();
    }

    /**
     * Initializes all the components needed for this panel.
     * 
     * @param frameWidth the width of the parent frame.
     */
    private void initializeComponents(int frameWidth) {
        buttonLayout = new FlowLayout();
        buttonLayout.setAlignment(FlowLayout.TRAILING);
        layout = new GridBagLayout();
        constraints = new GridBagConstraints();

        buttonPanel = new JPanel(buttonLayout);
        buttonPanel.setBackground(MainFrame.MAIN_COLOR);

        // headerPanel = new JPanel();
        this.setLayout(layout);
        this.setBackground(MainFrame.MAIN_COLOR);
        this.setPreferredSize(new Dimension(frameWidth, MainFrame.MESSAGE_PANEL_HEIGHT));
        this.setMinimumSize(this.getPreferredSize());
        this.setMaximumSize(this.getPreferredSize());

        userLabel = ComponentFactory.createLabel(friend.getUsername() + " (" + friend.getStringStatus() + ")");
        userLabel.setFont(HEADER_FONT);
        userLabel.setPreferredSize(new Dimension((frameWidth / 6) * 4, MainFrame.MESSAGE_PANEL_HEIGHT));
        userLabel.setMinimumSize(userLabel.getPreferredSize());

        picture = ComponentFactory.createImageLabel(friend.getPfp().getScaledInstance(32, 32, Image.SCALE_SMOOTH));
        picture.setPreferredSize(new Dimension(32, 32));
        picture.setMinimumSize(picture.getPreferredSize());
        picture.setMaximumSize(picture.getPreferredSize());

        picture.setBorder(null);
        userLabel.setBorder(null);

        // Show edit buttons if mouse is hovering
        MouseAdapter messageListener = new PanelMouseListener();
        this.addMouseListener(messageListener);

        chatButton = ComponentFactory.createImageButton("CHAT", "data/system/message.png", 16, 16, MainFrame.TEXT_COLOR,
                MainFrame.MAIN_COLOR, new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        initializeChatting();
                    }
                });
        chatButton.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        deleteButton = ComponentFactory.createImageButton("REMOVE FRIEND", "data/system/removeuser.png", 16, 16,
                MainFrame.TEXT_COLOR, MainFrame.MAIN_COLOR, new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        removeFriend();
                    }
                });
        deleteButton.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        buttonPanel.add(chatButton);
        buttonPanel.add(deleteButton);
    }

    /**
     * Reloads this panel, and re-adds existing components.
     */
    private void reload() {
        this.removeAll();

        DynamicGridbagFrame.addConstrainedComponent(picture, this, layout, constraints, 0, 0, 1, 1, 0.9, 0.9,
                GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, new Insets(0, 0, 0, 0));

        DynamicGridbagFrame.addConstrainedComponent(userLabel, this, layout, constraints, 1, 0, 1, 1, 0.9, 0.9,
                GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, new Insets(0, 0, 0, 0));

        DynamicGridbagFrame.addConstrainedComponent(buttonPanel, this, layout, constraints, 2, 0, 1, 1, 1.0, 1.0,
                GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, new Insets(1, 0, 2, 5));

        this.repaint();
        this.revalidate();
    }

    /**
     * Attempts to initialize chatting with a friend by sending a request to the
     * server.
     */
    private void initializeChatting() {
        String friendName = friend.getUsername();

        String newChannelName = client.getUser().getUsername() + " - " + friend.getUsername();
        String clientUsername = client.getUser().getUsername();
        client.offerEvent(new ChannelCreateEvent(clientUsername, -1, newChannelName, friendName, null));
    }

    /**
     * Attempts to remove a friend by sending a request to the server.
     */
    private void removeFriend() {
        String clientUsername = client.getUser().getUsername();
        String friendName = friend.getUsername();
        client.offerEvent(new FriendRemoveEvent(clientUsername, friendName));
    }

    /**
     * This class is a helper panel listener that provides mouse functionality if
     * the mouse enters or exist this panel.
     * <p>
     * Created <b>2020-12-12</b>
     * 
     * @since 1.0.0
     * @version 1.0.0
     * @author Joseph Wang
     */
    private final class PanelMouseListener extends MouseAdapter {
        public void mouseEntered(MouseEvent e) {
            setBackground(MainFrame.SIDE_COLOR);
            userLabel.setBackground(MainFrame.SIDE_COLOR);
            buttonPanel.setBackground(MainFrame.SIDE_COLOR);
        }

        public void mouseExited(MouseEvent e) {
            if (!contains(e.getPoint())) {
                setBackground(MainFrame.MAIN_COLOR);
                userLabel.setBackground(MainFrame.MAIN_COLOR);
                buttonPanel.setBackground(MainFrame.MAIN_COLOR);
            }
        }
    }
}