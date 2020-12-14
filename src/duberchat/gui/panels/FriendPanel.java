package duberchat.gui.panels;

import java.awt.event.*;
import java.util.HashSet;

import javax.swing.*;
import java.awt.*;

import duberchat.chatutil.Channel;
import duberchat.chatutil.Message;
import duberchat.chatutil.User;
import duberchat.client.ChatClient;
import duberchat.events.ChannelCreateEvent;
import duberchat.events.FriendRemoveEvent;
import duberchat.events.MessageDeleteEvent;
import duberchat.events.MessageEditEvent;
import duberchat.gui.frames.DynamicGridbagFrame;
import duberchat.gui.frames.MainFrame;
import duberchat.gui.util.ComponentFactory;
import duberchat.gui.filters.TextLengthFilter;

/**
 * [INSERT DESCRIPTION HERE]
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
    protected ChatClient client;
    private User friend;

    private JButton chatButton;
    private JButton deleteButton;

    private JLabel userLabel;
    private JLabel picture;

    private JPanel buttonPanel;
    private JPanel messagePanel;

    private FlowLayout buttonLayout;
    private GridBagLayout layout;
    private GridBagConstraints constraints;

    public FriendPanel(ChatClient client, User friend, int frameWidth) {
        super();

        this.client = client;
        this.friend = friend;

        this.initializeComponents(frameWidth);
        this.reload();
    }

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

        // new Dimension(editButton.getWidth() + deleteButton.getWidth() + 25,
        // MainFrame.MESSAGE_PANEL_HEIGHT));
        // buttonPanel.setMinimumSize(buttonPanel.getPreferredSize());

        buttonPanel.add(chatButton);
        buttonPanel.add(deleteButton);
    }

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

    private void initializeChatting() {
        String friendName = friend.getUsername();

        String newChannelName = "@" + friend.getUsername();
        String clientUsername = client.getUser().getUsername();
        client.offerEvent(new ChannelCreateEvent(clientUsername, -1, newChannelName, friendName, null));
    }

    private void removeFriend() {
        String clientUsername = client.getUser().getUsername();
        String friendName = friend.getUsername();
        client.offerEvent(new FriendRemoveEvent(clientUsername, friendName));
    }

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