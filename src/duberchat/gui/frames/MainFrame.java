package duberchat.gui.frames;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Font;
import java.awt.Image;
import java.awt.GridBagConstraints;
import java.awt.Toolkit;
import java.awt.Color;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowStateListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.io.File;
import java.io.IOException;

import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.UIManager;

import javax.imageio.ImageIO;
import duberchat.events.ChannelHierarchyChangeEvent;
import duberchat.events.ChannelAddMemberEvent;
import duberchat.events.ChannelRemoveMemberEvent;
import duberchat.events.ChannelDeleteEvent;
import duberchat.events.ChannelEvent;
import duberchat.events.MessageSentEvent;
import duberchat.events.MessageEvent;
import duberchat.events.ClientProfileUpdateEvent;
import duberchat.events.ClientRequestMessageEvent;
import duberchat.events.ClientEvent;
import duberchat.events.FriendAddEvent;
import duberchat.events.FriendEvent;
import duberchat.events.SerializableEvent;
import duberchat.gui.filters.TextLengthFilter;
import duberchat.gui.util.ComponentFactory;
import duberchat.gui.util.FrameFactory;
import duberchat.gui.panels.FriendPanel;
import duberchat.gui.panels.ChannelPanel;
import duberchat.gui.panels.MessagePanel;
import duberchat.gui.panels.UserPanel;
import duberchat.client.ChatClient;

import duberchat.chatutil.Channel;
import duberchat.chatutil.Message;
import duberchat.chatutil.User;

/**
 * The {@code MainFrame} contains all the main GUI elements as well as all the
 * main logic and methods behind the main menu screen.
 * <p>
 * Many child frames and child panels stem from this main frame. This main frame
 * should be launched upon login success, and handles most gui elements and all
 * main gui logic.
 * <p>
 * In order to keep a steady sync and keep displayed components constantly
 * updated, reloads to this frame are common, and new components may be made to
 * replace old, outdated components. This frame contains many listeners,
 * intended to assist with keeping components updated, but none of these
 * listeners will consume the event.
 * <p>
 * Created <b> 2020-12-09 </b>
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Joseph Wang
 */
@SuppressWarnings("serial")
public class MainFrame extends DynamicFrame {
    /** The height of a message panel, in pixels. */
    public static final int MESSAGE_PANEL_HEIGHT = 45;
    /** The height of a channel panel, in pixels. */
    public static final int SIDE_PANEL_HEIGHT = 50;
    /** The default size of this frame. */
    public static final Dimension DEFAULT_SIZE = Toolkit.getDefaultToolkit().getScreenSize();

    /** The main background color of this application. */
    public static final Color MAIN_COLOR = new Color(60, 60, 60);
    /** An alternative "panel color" of this application. */
    public static final Color PANEL_COLOR = new Color(50, 50, 50);
    /** An alternative "side color" of this application. */
    public static final Color SIDE_COLOR = new Color(40, 40, 40);
    /** An alternative "dark side color" of this application. */
    public static final Color DARK_SIDE_COLOR = new Color(20, 20, 20);
    /** The main textbox color of this application. */
    public static final Color TEXTBOX_COLOR = new Color(80, 80, 80);
    /** An alternative "dark textbox color" of this application. */
    public static final Color DARK_TEXTBOX_COLOR = new Color(40, 40, 40);
    /** The main text color of this application. */
    public static final Color TEXT_COLOR = new Color(150, 150, 150);
    /** An alternative "secondary text color" of this application. */
    public static final Color SECONDARY_TEXT_COLOR = new Color(180, 180, 180);
    /** An alternative "bright text color" of this application. */
    public static final Color BRIGHT_TEXT_COLOR = new Color(220, 220, 220);

    /** A heading font for the important messages in this frame. */
    public static final Font HEADING_FONT = new Font("Courier", Font.BOLD, 16);

    /** The maximum width a channel can be. */
    private int maxChannelWidth = DEFAULT_SIZE.width / 7;
    /**
     * The max amount of side panels that can be added to the side panels without
     * squishing.
     */
    private int maxSidePanelGrids = DEFAULT_SIZE.height / SIDE_PANEL_HEIGHT;
    /**
     * The max amount of message panels that can be added to the text panel without
     * squishing.
     */
    private int maxMessageGrids = DEFAULT_SIZE.height / MESSAGE_PANEL_HEIGHT;
    /** The index to load channels from, inclusive. */
    private int channelOffset = 0;
    /** The index to load users from, inclusive. */
    private int userOffset = 0;
    /** The index to load messages from, inclusive, to work with gridlayout. */
    private int messageOffset = 0;
    /** The index to load friends from, inclusive. */
    private int friendOffset = 0;

    /** The panel for all the channels. */
    private JPanel channelPanel;
    /** The panel for all the users. */
    private JPanel userPanel;
    /** The bottom panel with the input box. */
    private JPanel typingPanel;
    /** The top panel with profile/config options. */
    private JPanel configPanel;
    /** The panel for profile-related components. */
    private JPanel profileConfigPanel;
    /** The panel for all channel-related config components. */
    private JPanel channelConfigPanel;
    /** The panel for all the messages/friends. */
    private JPanel textPanel;
    /** The panel for the side channel create and home buttons. */
    private JPanel sideButtonPanel;

    /** This frame's custom frame for adding channels. */
    private ChannelCreateFrame addChannelFrame;
    /** This frame's custom frame for editing this user's profile information. */
    private ProfileFrame profileFrame;

    /** The profile label with the user's username. */
    private JLabel profileLabel;
    /** The profile button, which opens up a new ProfileFrame. */
    private JButton profileButton;
    /** The send button, which will send a message. */
    private JButton sendButton;
    /** The quit button, which will quit this application. */
    private JButton quitButton;
    /** The add user button, which will add a user to the current channel. */
    private JButton addUserButton;
    /**
     * The delete user button, which will delete a user from the current channel.
     */
    private JButton deleteUserButton;
    /** The add channel button, which will add a new channel. */
    private JButton addChannelButton;
    /** The delete channel button, which will delete the current channel. */
    private JButton deleteChannelButton;
    /**
     * The home button, which will send the user back to the home page (with
     * friends).
     */
    private JButton homeButton;
    /** The leave channel button, which makes the user leave the channel. */
    private JButton leaveChannelButton;
    /** The add friend button, which adds a friend. */
    private JButton addFriendButton;
    /** The label that indicates and describes the friend page. */
    private JLabel friendsLabel;
    /** The type field to type messages in. */
    private JTextField typeField;

    /**
     * The channel indicator, which tells the user what current channel they are in.
     */
    private JLabel channelIndicator;

    /** The associated client. */
    protected ChatClient client;

    /** Whether this client has already requested messages from this frame. */
    private boolean requestedMessages = false; // important to prevent event spam

    /**
     * Constructs a new {@code MainFrame}.
     * 
     * @param client the associated client.
     */
    public MainFrame(ChatClient client) {
        super("duberchat");
        this.client = client;

        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                setExtendedState(JFrame.ICONIFIED);
            }
        });
        this.setSize(MainFrame.DEFAULT_SIZE);
        this.setResizable(true);
        this.setIconImage(new ImageIcon("data/system/logo.png").getImage());
        this.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent evt) {
                reinitializeLayouts();
            }

            public void componentShown(ComponentEvent evt) {
                reinitializeLayouts();
            }
        });
        this.addWindowStateListener(new WindowStateListener() {
            public void windowStateChanged(WindowEvent evt) {
                if ((evt.getNewState() & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH) {
                    reinitializeLayouts();
                }
            }
        });

        // Initialize defaults
        this.initializeDefaultColours();
        this.initializeComponents(client);

        GridBagLayout typingPanelLayout = new GridBagLayout();
        typingPanel.setLayout(typingPanelLayout);
        configPanel.setLayout(new GridLayout(1, 2));
        channelPanel.setLayout(new GridLayout(this.maxSidePanelGrids, 1));
        userPanel.setLayout(new GridLayout(this.maxSidePanelGrids, 1));
        textPanel.setLayout(new GridLayout(this.maxMessageGrids, 1));
        profileConfigPanel.setLayout(new FlowLayout(FlowLayout.LEADING, 25, 5));
        channelConfigPanel.setLayout(new FlowLayout(FlowLayout.TRAILING, 25, 15));
        sideButtonPanel.setLayout(new GridLayout(2, 1));
        sideButtonPanel.add(homeButton);
        sideButtonPanel.add(addChannelButton);
        configPanel.add(profileConfigPanel);
        configPanel.add(channelConfigPanel);

        // Implement scrolling for each panel
        channelPanel.addMouseWheelListener(new ChannelMouseWheelListener(client));
        userPanel.addMouseWheelListener(new UserMouseWheelListener(client));
        textPanel.addMouseWheelListener(new MessageMouseWheelListener(client));
        textPanel.addMouseWheelListener(new FriendsMouseWheelListener(client));

        GridBagConstraints gbc = new GridBagConstraints();
        DynamicGridbagFrame.addConstrainedComponent(typeField, typingPanel, typingPanelLayout, gbc, 0, 0, 5, 1, 1.0,
                1.0, GridBagConstraints.BOTH, GridBagConstraints.CENTER, new Insets(10, 20, 10, 20));
        DynamicGridbagFrame.addConstrainedComponent(sendButton, typingPanel, typingPanelLayout, gbc, 5, 0, 1, 1, 0.5,
                1.0, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER, new Insets(10, 20, 10, 20));
        DynamicGridbagFrame.addConstrainedComponent(channelIndicator, typingPanel, typingPanelLayout, gbc, 0, 1, 5, 1,
                1.0, 1.0, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER, new Insets(0, 20, 0, 20));
        DynamicGridbagFrame.addConstrainedComponent(quitButton, typingPanel, typingPanelLayout, gbc, 5, 1, 1, 1, 0.5,
                1.0, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER, new Insets(0, 20, 10, 20));

        this.add(BorderLayout.WEST, channelPanel);
        this.add(BorderLayout.EAST, userPanel);
        this.add(BorderLayout.NORTH, configPanel);
        this.add(BorderLayout.CENTER, textPanel);
        this.add(BorderLayout.SOUTH, typingPanel);

        this.reload();
    }

    /**
     * Initialize default colours for each necessary component.
     * <p>
     * This helps us save some time on component factory methods, as the factory
     * method will use the default colour if one is not provided.
     */
    private void initializeDefaultColours() {
        UIManager.put("OptionPane.background", MAIN_COLOR);
        UIManager.getLookAndFeelDefaults().put("OptionPane.background", MAIN_COLOR);
        UIManager.put("Button.background", TEXT_COLOR);
        UIManager.getLookAndFeelDefaults().put("Button.background", TEXT_COLOR);
        UIManager.put("Button.foreground", MAIN_COLOR);
        UIManager.getLookAndFeelDefaults().put("Button.foreground", MAIN_COLOR);
        UIManager.put("Label.foreground", BRIGHT_TEXT_COLOR);
        UIManager.getLookAndFeelDefaults().put("Label.foreground", BRIGHT_TEXT_COLOR);
        UIManager.put("TextField.background", DARK_TEXTBOX_COLOR);
        UIManager.getLookAndFeelDefaults().put("TextField.background", DARK_TEXTBOX_COLOR);
        UIManager.put("TextField.foreground", BRIGHT_TEXT_COLOR);
        UIManager.getLookAndFeelDefaults().put("TextField.foreground", BRIGHT_TEXT_COLOR);
    }

    /**
     *
     * Reinitializes all responsive layouts using the current screen height.
     * <p>
     * This allows for us to make our chat gui responsive and sizable without
     * breaking.
     */
    private void reinitializeLayouts() {
        channelPanel.removeAll();
        userPanel.removeAll();
        textPanel.removeAll();

        this.maxSidePanelGrids = channelPanel.getHeight() / SIDE_PANEL_HEIGHT;
        this.maxChannelWidth = this.getWidth() / 7;
        this.maxMessageGrids = textPanel.getHeight() / MESSAGE_PANEL_HEIGHT;

        channelPanel.setLayout(new GridLayout(this.maxSidePanelGrids, 1));
        userPanel.setLayout(new GridLayout(this.maxSidePanelGrids, 1));
        textPanel.setLayout(new GridLayout(this.maxMessageGrids, 1));

        this.reload();
    }

    /**
     * Initializes all the required components for this frame.
     */
    private void initializeComponents(ChatClient client) {
        // INITIALIZE PANELS ====================================================
        typingPanel = new JPanel();
        channelPanel = new JPanel();
        userPanel = new JPanel();
        configPanel = new JPanel();
        profileConfigPanel = new JPanel();
        channelConfigPanel = new JPanel();
        textPanel = new JPanel();
        sideButtonPanel = new JPanel();

        channelPanel.setBackground(SIDE_COLOR);
        sideButtonPanel.setBackground(channelPanel.getBackground());
        userPanel.setBackground(SIDE_COLOR);
        typingPanel.setBackground(SIDE_COLOR);
        profileConfigPanel.setBackground(DARK_SIDE_COLOR);
        channelConfigPanel.setBackground(DARK_SIDE_COLOR);
        textPanel.setBackground(MAIN_COLOR);

        // INITIALIZE BUTTONS ====================================================
        String profileText = this.client.getUser().getUsername() + " (" + this.client.getUser().getStringStatus() + ")";
        profileLabel = ComponentFactory.createLabel(profileText, TEXT_COLOR);
        profileLabel.setFont(HEADING_FONT);
        profileButton = ComponentFactory.createButton("", TEXT_COLOR, PANEL_COLOR);
        profileButton.setBorder(null);
        profileButton.setIcon(new ImageIcon(client.getUser().getPfp().getScaledInstance(48, 48, Image.SCALE_SMOOTH)));
        profileButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                displayProfile();
            }
        });
        profileConfigPanel.add(profileButton);
        profileConfigPanel.add(profileLabel);

        // Create delete channel button
        deleteChannelButton = ComponentFactory.createImageButton("DELETE CHANNEL", "data/system/trash.png", 20, 20,
                TEXT_COLOR, PANEL_COLOR);
        deleteChannelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                confirmDeletingChannel();
            }
        });

        // Create add user button
        addUserButton = ComponentFactory.createImageButton("ADD USER", "data/system/adduser.png", 20, 20, TEXT_COLOR,
                PANEL_COLOR);
        addUserButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                selectUserToAdd();
            }
        });

        // Create delete user button
        deleteUserButton = ComponentFactory.createImageButton("REMOVE USER", "data/system/removeuser.png", 20, 20,
                TEXT_COLOR, PANEL_COLOR);
        deleteUserButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                selectUserToRemove();
            }
        });

        // Create send button
        sendButton = ComponentFactory.createButton("SEND", MAIN_COLOR, TEXT_COLOR);
        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (client.getCurrentChannel() == null) {
                    return;
                }
                sendMessage();
            }
        });

        // Create quit button
        quitButton = ComponentFactory.createButton("QUIT", MAIN_COLOR, TEXT_COLOR, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                client.initiateShutdown();
                destroy();
            }
        });

        // Create leave channel button
        leaveChannelButton = ComponentFactory.createImageButton("LEAVE", "data/system/leave.png", 20, 20, TEXT_COLOR,
                PANEL_COLOR);
        leaveChannelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                confirmleavingChannel();
            }
        });

        // Create add channel button
        addChannelButton = ComponentFactory.createImageButton("CREATE CHANNEL", "data/system/plus sign.png", 20, 20,
                TEXT_COLOR, PANEL_COLOR);
        addChannelButton.setPreferredSize(new Dimension(maxChannelWidth, SIDE_PANEL_HEIGHT));
        addChannelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (hasActiveChannelCreateFrame()) {
                    return;
                }

                addChannelFrame = new ChannelCreateFrame(client);
                addChannelFrame.setVisible(true);
            }
        });

        // Create home button
        homeButton = ComponentFactory.createImageButton("HOME", "data/system/home.png", 20, 20, MAIN_COLOR,
                PANEL_COLOR);
        homeButton.setPreferredSize(new Dimension(maxChannelWidth, SIDE_PANEL_HEIGHT));
        homeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                client.setCurrentChannel(null);

                switchChannelsToCurrent();
            }
        });

        // INITIALIZE FRIENDS =================================================
        // We don't use createImageLabel because we want both the text and image
        friendsLabel = ComponentFactory.createLabel("Friends", TEXT_COLOR);
        friendsLabel.setFont(HEADING_FONT);
        try {
            BufferedImage image = ImageIO.read(new File("data/system/user.png"));
            friendsLabel.setIcon(new ImageIcon(image.getScaledInstance(16, 16, Image.SCALE_SMOOTH)));
        } catch (IOException e) {
            System.out.println("SYSTEM: Could not load friend image!");
        }

        // create add friend button
        addFriendButton = ComponentFactory.createImageButton("ADD USER", "data/system/adduser.png", 20, 20, TEXT_COLOR,
                PANEL_COLOR);
        addFriendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                selectUserToBefriend();
            }
        });

        // INITIALIZE TYPING AREA =================================================
        typeField = ComponentFactory.createTextBox(20, BRIGHT_TEXT_COLOR, TEXTBOX_COLOR,
                new TextLengthFilter(Message.MAX_LENGTH));
        typeField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (client.getCurrentChannel() == null) {
                        return;
                    }

                    sendMessage();
                }
            }
        });

        channelIndicator = ComponentFactory.createLabel("No channel selected.", SECONDARY_TEXT_COLOR);
        channelIndicator.setFont(HEADING_FONT);
    }

    /**
     * {@inheritDoc}
     * <p>
     * As a direct result of receiving a source, this frame overrides the default
     * reload method and only reloads impacted components. This allows the reload
     * process to be as fast as possible and this reload should be used when
     * possible.
     * <p>
     * If a generic reload for all components is requested, consider using the
     * {@link #reload() other reload method}.
     * 
     * @param source {@inheritDoc}
     */
    public synchronized void reload(SerializableEvent source) {
        if (source instanceof MessageEvent) {
            this.reloadMessages();

        } else if (source instanceof ChannelAddMemberEvent) {
            this.reloadUsers();
            this.reloadChannels();

        } else if (source instanceof ChannelRemoveMemberEvent) {
            this.reloadUsers();
            this.reloadChannels();
            this.reloadMessages();

        } else if (source instanceof ChannelHierarchyChangeEvent) {
            this.reloadUsers();
            this.reloadMessages();

        } else if (source instanceof ClientProfileUpdateEvent) {
            this.reloadUsers();
            this.reloadFriends(); // to deal with friend discrepancies
            this.reloadMessages(); // to deal with message discrepancies (pfp)
            this.reloadProfile();

        } else if (source instanceof ClientRequestMessageEvent) {
            this.resetRequestedMessages();
            this.reloadMessages();

        } else if (source instanceof ClientEvent) {
            this.reloadUsers();

        } else if (source instanceof ChannelEvent) {
            this.reload();
            return;

        } else if (source instanceof FriendEvent) {
            this.reloadFriends();

        } else {
            this.reload();
            return;
        }

        this.repaint();
        this.revalidate();
    }

    /**
     * {@inheritDoc}
     * <p>
     * As a direct result of not receiving a source, this method will force all
     * elements to reload. This is useful if the client requires all components to
     * reload, but is more costly than specific reloads and should not be used for
     * common events.
     */
    public synchronized void reload() {
        if (client.getCurrentChannel() != null) {
            // Remove aspects that need a current channel and reset for friends
            typeField.setEditable(true);
            typeField.setText("");
            channelIndicator.setText("Channel: " + client.getCurrentChannel().getChannelName());
        } else {
            // Remove friends stuff and add channel specific components
            typeField.setText("Messaging Disabled...");
            typeField.setEditable(false);
            channelIndicator.setText("No channel selected.");
        }

        this.reloadMessages();
        this.reloadUsers();
        this.reloadChannels();
        this.reloadFriends();

        this.repaint();
        this.revalidate();
    }

    /**
     * Reloads this frame's small profile picture component.
     * <p>
     * Only significant if the profile picture has changed.
     */
    private synchronized void reloadProfile() {
        String profileText = this.client.getUser().getUsername() + " (" + this.client.getUser().getStringStatus() + ")";
        profileLabel.setText(profileText);
        profileButton.setIcon(new ImageIcon(client.getUser().getPfp().getScaledInstance(48, 48, Image.SCALE_SMOOTH)));
    }

    /**
     * Reloads this frame's displayed message panels.
     * <p>
     * This method should be called whenever a channel's messages is edited in any
     * way to ensure client has the most recent and most updated mesesages. This
     * method will create new method panels and leave the old ones out for garbage
     * collection.
     */
    private synchronized void reloadMessages() {
        if (client.getCurrentChannel() == null) {
            return;
        }

        textPanel.removeAll();

        Channel sourceChannel = client.getCurrentChannel();
        ArrayList<Message> messages = sourceChannel.getMessages();
        LinkedHashMap<String, User> users = sourceChannel.getUsers();
        HashSet<User> adminUsers = sourceChannel.getAdminUsers();

        for (int messageIndex = 0; messageIndex < messages.size(); messageIndex++) {
            // Only display messages within the max message grids range, taking account
            // message offset.
            if (messageIndex >= messages.size() - messageOffset
                    || messageIndex < (messages.size() - maxMessageGrids) - messageOffset) {
                continue;
            }

            Message msg = messages.get(messageIndex);
            User sender = users.get(msg.getSenderUsername());

            boolean showAdmin = adminUsers.contains(client.getUser());
            boolean showHeader = false;

            // If this message is the first message or is sent by a new person, show a
            // header.
            if (messageIndex != 0) {
                if (!(msg.getSenderUsername().equals(messages.get(messageIndex - 1).getSenderUsername()))) {
                    showHeader = true;
                }
            } else {
                showHeader = true;
            }

            textPanel.add(new MessagePanel(client, sender, msg, showHeader, showAdmin, textPanel.getWidth()));
        }
    }

    /**
     * Reloads this frame's displayed user panels.
     * <p>
     * This method should be called whenever a change to a channel's users occur, in
     * order to keep the users updated. This method will create new user panels and
     * leave the old ones out for garbage collection.
     */
    private synchronized void reloadUsers() {
        userPanel.removeAll();
        
        if (client.getCurrentChannel() == null) {
            return;
        }

        // Update channel config panel buttons
        channelConfigPanel.removeAll();
        channelConfigPanel.add(leaveChannelButton);
        channelConfigPanel.add(addUserButton);
        if (client.getCurrentChannel().getAdminUsers().contains(client.getUser())) {
            channelConfigPanel.add(deleteUserButton);
            channelConfigPanel.add(deleteChannelButton);
        }

        Channel curChannel = client.getCurrentChannel();

        Iterator<User> userIterator = client.getCurrentChannel().getUsers().values().iterator();
        for (int userIndex = 0; userIterator.hasNext(); userIndex++) {
            User u = userIterator.next();

            // Only show users that fall within the max side panel grids range, taking into
            // account useroffset.
            if (userIndex < userOffset || userIndex >= maxSidePanelGrids + userOffset) {
                continue;
            }

            userPanel.add(new UserPanel(client, u, curChannel.getAdminUsers().contains(u)));
            // Limit the size of the panel so it doesn't take too much space.
            userPanel.setMaximumSize(new Dimension(maxChannelWidth, SIDE_PANEL_HEIGHT));
        }
    }

    /**
     * Reloads this frame's displayed channel panels.
     * <p>
     * This method should be called whenever a change to the client's channels
     * occur, to keep this main menu updated. This method will create new channel
     * panels and leave the old ones out for garbage collection.
     */
    private synchronized void reloadChannels() {
        channelPanel.removeAll();

        if (maxSidePanelGrids >= 1) {
            addChannelButton.setPreferredSize(new Dimension(maxChannelWidth, SIDE_PANEL_HEIGHT));
            homeButton.setPreferredSize(new Dimension(maxChannelWidth, SIDE_PANEL_HEIGHT));
            channelPanel.add(sideButtonPanel);
        }

        if (this.client.getChannels().size() == 0) {
            return;
        }

        Iterator<Channel> channelIterator = this.client.getChannels().values().iterator();
        for (int channelIndex = 0; channelIterator.hasNext(); channelIndex++) {
            Channel c = channelIterator.next();

            // Only show channels in the max side panel grids range (minus one for the
            // buttons), keeping in mind channel offset.
            if (channelIndex < channelOffset || channelIndex >= (maxSidePanelGrids - 1) + channelOffset) {
                continue;
            }

            Color defaultColor = SIDE_COLOR;
            // written this way so that if the current channel is null, that's okay
            if (c.equals(this.client.getCurrentChannel())) {
                defaultColor = MAIN_COLOR;
            }

            channelPanel.add(new ChannelPanel(this.client, c, defaultColor));
            // Ensure channels don't get too large.
            channelPanel.setMaximumSize(new Dimension(maxChannelWidth, SIDE_PANEL_HEIGHT));
        }
    }

    /**
     * Reloads this frame's displayed friend panels, if any are displayed.
     * <p>
     * This method should be called whenever a change to the client's friends occur.
     * This keeps the displayed friends updated. This method will create new friend
     * panels and leave the old ones out for garbage collection.
     */
    private synchronized void reloadFriends() {
        // Friends panel share the text panel, but only when the currentChannel is null.
        if (client.hasCurrentChannel()) {
            return;
        }

        textPanel.removeAll(); // remove existing components, for garbage collection

        Iterator<User> friendIterator = this.client.getFriends().values().iterator();
        for (int friendIndex = 0; friendIterator.hasNext(); friendIndex++) {
            User friend = friendIterator.next();

            // Only show friends within the max message grid, keeping in mind friend offset
            if (friendIndex < friendOffset || friendIndex >= maxMessageGrids + friendOffset) {
                continue;
            }

            textPanel.add(new FriendPanel(this.client, friend, textPanel.getWidth()));
        }
    }

    /**
     * Exclusively reloads channels, and reverifies this frame.
     */
    private void onlyReloadChannels() {
        this.reloadChannels();

        this.repaint();
        this.revalidate();
    }

    /**
     * Exclusively reloads users, and reverifies this frame.
     */
    private void onlyReloadUsers() {
        this.reloadUsers();

        this.repaint();
        this.revalidate();
    }

    /**
     * Exclusively reloads messages, and reverifies this frame.
     */
    private void onlyReloadMessages() {
        this.reloadMessages();

        this.repaint();
        this.revalidate();
    }

    /**
     * Exclusively reloads friends, and reverifies this frame.
     */
    private void onlyReloadFriends() {
        this.reloadFriends();

        this.repaint();
        this.revalidate();
    }

    /**
     * Checks if this frame has an active {@code ChannelCreateFrame}.
     * 
     * @return true if this frame has an active {@code ChannelCreateFrame}.
     */
    public boolean hasActiveChannelCreateFrame() {
        return (this.addChannelFrame != null && this.addChannelFrame.isVisible());
    }

    /**
     * Checks if this frame has an active {@code ProfileFrame}.
     * 
     * @return true if this frame has an active {@code ProfileFrame}.
     */
    public boolean hasActiveProfileFrame() {
        return (this.profileFrame != null && this.profileFrame.isVisible());
    }

    /**
     * Retrieves this frame's {@code ChannelCreateFrame}.
     * 
     * @return this frame's {@code ChannelCreateFrame}.
     */
    public ChannelCreateFrame getChannelCreateFrame() {
        return this.addChannelFrame;
    }

    /**
     * Retrieves this frame's {@code ProfileFrame}.
     * 
     * @return this frame's {@code ProfileFrame}.
     */
    public ProfileFrame getProfileFrame() {
        return this.profileFrame;
    }

    /**
     * Attempts to close this frame's {@code ChannelCreateFrame}, and returns a
     * boolean based on success.
     * 
     * @return true if this channel's {@code ChannelCreateFrame} was successfully
     *         closed.
     */
    public boolean closeChannelCreateFrame() {
        if (this.addChannelFrame == null) {
            return false;
        }

        this.addChannelFrame.destroy();
        return true;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Destroying this frame will also destroy any subframes.
     */
    public void destroy() {
        if (this.hasActiveChannelCreateFrame()) {
            this.addChannelFrame.dispose();
        }

        if (this.hasActiveProfileFrame()) {
            this.profileFrame.dispose();
        }

        this.dispose();
        System.exit(0);
    }

    /**
     * Resets this frame's requestedMessages flag, allowing the client to request
     * more messages again.
     */
    public void resetRequestedMessages() {
        this.requestedMessages = false;
    }

    /**
     * Swaps this frame's currently displaying channel to the client's current
     * channel, and reloads all the side components.
     */
    public void switchChannelsToCurrent() {
        this.messageOffset = 0;
        this.reload();
        this.resetRequestedMessages();
    }

    /**
     * Attempts to send a message to the server.
     */
    private void sendMessage() {
        if (typeField.getText() == "") {
            return;
        }

        String clientUsername = client.getUser().getUsername();
        String timestamp = new Date().toString();
        int channelId = client.getCurrentChannel().getChannelId();
        Message msg = new Message(typeField.getText(), clientUsername, -1, timestamp, channelId);
        client.offerEvent(new MessageSentEvent(clientUsername, msg));
        typeField.setText("");

        System.out.println("SYSTEM: Sent message " + typeField.getText());
    }

    /**
     * Adds a specified user to the client's current channel.
     * 
     * @param user the username of the user to add.
     */
    private void addUserToChannel(String user) {
        if (user.equals("") || user.equals(client.getUser().getUsername())) {
            return;
        }

        String userToAdd = user.replaceFirst("@", "");
        String clientUsername = client.getUser().getUsername();
        int curChannelId = client.getCurrentChannel().getChannelId();

        client.offerEvent(new ChannelAddMemberEvent(clientUsername, curChannelId, userToAdd));
    }

    /**
     * Creates and displays a request frame for the user, prompting them to enter
     * their new friend's username.
     * <p>
     * After confirming the friend name, this method will attempt to send an event
     * to the server to add a friend.
     */
    private void selectUserToBefriend() {
        JLabel label = ComponentFactory.createLabel("Add a friend's username, starting with @ (eg. @EmeraldPhony)",
                BRIGHT_TEXT_COLOR);
        JTextField input = ComponentFactory.createTextBox(20);
        JButton submit = ComponentFactory.createButton("Add Friend!", new ActionListener() {
            public void actionPerformed(ActionEvent evt2) {
                addFriend(input.getText());
            }
        });

        JFrame addFriend = FrameFactory.createRequestFrame("Add friend", MAIN_COLOR, label, input, submit);
        addFriend.setVisible(true);
    }

    /**
     * Adds a specified user to the client's friend list.
     * 
     * @param user the username of the user to add.
     */
    private void addFriend(String user) {
        if (user.equals("") || user.equals(client.getUser().getUsername())) {
            return;
        }

        String clientUsername = client.getUser().getUsername();
        String userToBefriend = user.replaceFirst("@", "");
        client.offerEvent(new FriendAddEvent(clientUsername, userToBefriend));
    }

    /**
     * Ensures a profile frame is being displayed to the user, if there is not one
     * already.
     */
    private void displayProfile() {
        if (!hasActiveProfileFrame()) {
            profileFrame = new ProfileFrame(client);
            profileFrame.setVisible(true);
        }
    }

    /**
     * Prompts the user as to whether they want to delete the current channel or
     * not.
     * <p>
     * If the user presses yes, sends a channel delete event to the server.
     */
    private void confirmDeletingChannel() {
        JLabel label = ComponentFactory.createLabel("Are you sure you want to delete this channel?", BRIGHT_TEXT_COLOR);
        ActionListener action = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                deleteCurrentChannel();
            }
        };

        JFrame deleteChannel = FrameFactory.createConfirmFrame("Delete channel", MAIN_COLOR, label, action);
        deleteChannel.setVisible(true);
    }

    /**
     * Deletes the client's current channel and sends a channel delete event to the
     * server.
     */
    private void deleteCurrentChannel() {
        String clientUsername = client.getUser().getUsername();
        int curChannelId = client.getCurrentChannel().getChannelId();
        client.offerEvent(new ChannelDeleteEvent(clientUsername, curChannelId));
    }

    /**
     * Prompts client to input a selected user to add to the current channel.
     * <p>
     * Once the client submits, sends a user add event to the server.
     */
    private void selectUserToAdd() {
        JLabel label = ComponentFactory.createLabel("Add a username to add, starting with @ (eg. @EmeraldPhony)",
                BRIGHT_TEXT_COLOR);
        JTextField input = ComponentFactory.createTextBox(20);
        JButton submit = ComponentFactory.createButton("Add User", new ActionListener() {
            public void actionPerformed(ActionEvent evt2) {
                addUserToChannel(input.getText());
            }
        });

        JFrame addUser = FrameFactory.createRequestFrame("Add user", MAIN_COLOR, label, input, submit);
        addUser.setVisible(true);
    }

    /**
     * Prompts client to input a selected user to remove from the current channel.
     * <p>
     * Once the client submits, sends a user remove event to the server.
     */
    private void selectUserToRemove() {
        JLabel label = ComponentFactory.createLabel("Add a username to remove, starting with @ (eg. @EmeraldPhony)",
                BRIGHT_TEXT_COLOR);
        JTextField input = ComponentFactory.createTextBox(20);
        JButton submit = ComponentFactory.createButton("Remove User", new ActionListener() {
            public void actionPerformed(ActionEvent evt2) {
                removeUser(input.getText());
            }
        });

        JFrame removeUser = FrameFactory.createRequestFrame("Remove user", MAIN_COLOR, label, input, submit);
        removeUser.setVisible(true);
    }

    /**
     * Removes a specified user from the channel, and sends a user remove event to
     * the server.
     * <p>
     * If the provided username is of the client's user, or is "", returns early.
     * 
     * @param user the username of the user to remove.
     */
    private void removeUser(String user) {
        if (user.equals("")) {
            return;
        }

        String username = user.replaceFirst("@", "");
        User removedUser = client.getCurrentChannel().getUsers().get(username);

        if (removedUser == null || client.getUser().equals(removedUser)) {
            return;
        }

        String clientUsername = client.getUser().getUsername();
        int curChannelId = client.getCurrentChannel().getChannelId();
        client.offerEvent(new ChannelRemoveMemberEvent(clientUsername, curChannelId, username));
    }

    /**
     * Prompts the client as to whether they want to leave the channel or not.
     * <p>
     * If the client selects yes, sends a channel remove event with the client's
     * user to the server.
     */
    private void confirmleavingChannel() {
        JLabel label = ComponentFactory.createLabel("Are you sure you want to leave this channel?", BRIGHT_TEXT_COLOR);
        ActionListener action = new ActionListener() {
            public void actionPerformed(ActionEvent evt2) {
                leaveChannel();
            }
        };

        JFrame removeConfirmPanel = FrameFactory.createConfirmFrame("Delete channel", MAIN_COLOR, label, action);
        removeConfirmPanel.setVisible(true);
    }

    /**
     * Removes the client from the current channel and sends a channel remove event
     * to the server with the client's user.
     */
    private void leaveChannel() {
        // This button will only exist if a current channel exists
        String clientUsername = client.getUser().getUsername();
        int curChannelId = client.getCurrentChannel().getChannelId();
        client.offerEvent(new ChannelRemoveMemberEvent(clientUsername, curChannelId, clientUsername));
    }

    /**
     * The {@code FriendsMouseWheelListener} listener provides functionality for
     * friend panel scrolling and reloading.
     * <p>
     * This {@code MouseWheelListener} will adjust the {@code friendOffset} field,
     * which allows for scrolling.
     * <p>
     * Created <b> 2020-12-11 </b>
     * 
     * @since 1.0.0
     * @version 1.0.0
     * @author Joseph Wang
     * @see java.awt.event.MouseWheelListener
     */
    private class FriendsMouseWheelListener implements MouseWheelListener {
        /** The associated client. */
        private ChatClient client;

        /**
         * Constructs a new {@code FriendsMouseWheelListener}.
         * 
         * @param client the associated client.
         */
        private FriendsMouseWheelListener(ChatClient client) {
            this.client = client;
        }

        public void mouseWheelMoved(MouseWheelEvent e) {
            if (client.hasCurrentChannel()) {
                return;
            }

            // Scrolls up
            if (e.getWheelRotation() < 0) {
                if (friendOffset > 0) {
                    friendOffset--;
                    onlyReloadFriends();
                }
            } else {
                // Scrolls down
                // Cap the scrolling at a point
                if (client.getFriends().size() > maxMessageGrids
                        && client.getFriends().size() - userOffset > maxSidePanelGrids) {
                    friendOffset++;
                    onlyReloadFriends();
                }
            }
        }
    }

    /**
     * The {@code MessageMouseWheelListener} listener provides functionality for
     * message panel scrolling and reloading.
     * <p>
     * This {@code MouseWheelListener} will adjust the {@code messageOffset} field,
     * which allows for scrolling of message panels.
     * <p>
     * Created <b> 2020-12-11 </b>
     * 
     * @since 1.0.0
     * @version 1.0.0
     * @author Joseph Wang
     * @see java.awt.event.MouseWheelListener
     */
    private class MessageMouseWheelListener implements MouseWheelListener {
        /** The associated client. */
        private ChatClient client;

        /**
         * Constructs a new {@code MessageMouseWheelListener}.
         * 
         * @param client the associated client.
         */
        private MessageMouseWheelListener(ChatClient client) {
            this.client = client;
        }

        public void mouseWheelMoved(MouseWheelEvent e) {
            if (!client.hasCurrentChannel()) {
                return;
            }

            // scroll down
            if (e.getWheelRotation() > 0) {
                if (messageOffset > 0) {
                    messageOffset--;
                    onlyReloadMessages();
                }
            } else {
                // scroll up
                if (!client.hasCurrentChannel()) {
                    return;
                }

                Channel curChannel = client.getCurrentChannel();
                // if there are still messages, load them
                if (curChannel.getMessages().size() > maxMessageGrids
                        && curChannel.getMessages().size() - messageOffset > maxMessageGrids) {
                    messageOffset++;
                    onlyReloadMessages();

                    // otherwise request more messages from the server
                } else if (curChannel.getMessages().size() >= Channel.LOCAL_SAVE_AMT
                        && (curChannel.getMessages().size() - messageOffset) <= maxMessageGrids && !requestedMessages) {

                    String clientUsername = client.getUser().getUsername();
                    int startId = curChannel.getMessages().get(0).getMessageId();
                    int channelId = curChannel.getChannelId();

                    client.offerEvent(new ClientRequestMessageEvent(clientUsername, startId, channelId));
                    requestedMessages = true;
                }
            }
        }
    }

    /**
     * The {@code UserMouseWheelListener} listener provides functionality for user
     * panel scrolling and reloading.
     * <p>
     * This {@code MouseWheelListener} will adjust the {@code userOffset} field,
     * which allows for scrolling of user panels.
     * <p>
     * Created <b> 2020-12-11 </b>
     * 
     * @since 1.0.0
     * @version 1.0.0
     * @author Joseph Wang
     * @see java.awt.event.MouseWheelListener
     */
    private class UserMouseWheelListener implements MouseWheelListener {
        /** The associated client. */
        private ChatClient client;

        /**
         * Constructs a new {@code UserMouseWheelListener}.
         * 
         * @param client the associated client.
         */
        private UserMouseWheelListener(ChatClient client) {
            this.client = client;
        }

        public void mouseWheelMoved(MouseWheelEvent e) {
            // scroll up
            if (e.getWheelRotation() < 0) {
                if (userOffset > 0) {
                    userOffset--;
                    onlyReloadUsers();
                }
            } else {
                // scroll down
                // If the user has no current channel, there are no users to show and no offset
                // to adjust
                if (!client.hasCurrentChannel()) {
                    return;
                }

                if (client.getCurrentChannel().getUsers().size() > maxSidePanelGrids
                        && client.getCurrentChannel().getUsers().size() - userOffset > maxSidePanelGrids) {
                    userOffset++;
                    onlyReloadUsers();
                }
            }
        }
    }

    /**
     * The {@code ChannelMouseWheelListener} listener provides functionality for
     * channel panel scrolling and reloading.
     * <p>
     * This {@code MouseWheelListener} will adjust the {@code channelOffset} field,
     * which allows for scrolling of channel panels.
     * <p>
     * Created <b> 2020-12-11 </b>
     * 
     * @since 1.0.0
     * @version 1.0.0
     * @author Joseph Wang
     * @see java.awt.event.MouseWheelListener
     */
    private class ChannelMouseWheelListener implements MouseWheelListener {
        /** The associated client. */
        private ChatClient client;

        /**
         * Constructs a new {@code ChannelMouseWheelListener}.
         * 
         * @param client the associated client.
         */
        private ChannelMouseWheelListener(ChatClient client) {
            this.client = client;
        }

        public void mouseWheelMoved(MouseWheelEvent e) {
            // scroll up
            if (e.getWheelRotation() < 0) {
                if (channelOffset > 0) {
                    channelOffset--;
                    onlyReloadChannels();
                }
            } else {
                // scroll down
                if (channelOffset < client.getChannels().size() - 1) {
                    channelOffset++;
                    onlyReloadChannels();
                }
            }
        }
    }
}
