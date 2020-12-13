package duberchat.gui.frames;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Date;
import java.util.Iterator;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import duberchat.events.*;
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

@SuppressWarnings("serial")
public class MainFrame extends DynamicFrame {
    /** The height of a message panel, in pixels. */
    public static final int MESSAGE_PANEL_HEIGHT = 45;
    /** The height of a channel panel, in pixels. */
    public static final int SIDE_PANEL_HEIGHT = 50;
    public static final Dimension DEFAULT_SIZE = Toolkit.getDefaultToolkit().getScreenSize();

    public static final Color MAIN_COLOR = new Color(60, 60, 60);
    public static final Color PANEL_COLOR = new Color(50, 50, 50);
    public static final Color SIDE_COLOR = new Color(40, 40, 40);
    public static final Color DARK_SIDE_COLOR = new Color(20, 20, 20);
    public static final Color DARK_TEXTBOX_COLOR = new Color(40, 40, 40);
    public static final Color TEXTBOX_COLOR = new Color(80, 80, 80);
    public static final Color TEXT_COLOR = new Color(150, 150, 150);
    public static final Color SECONDARY_TEXT_COLOR = new Color(180, 180, 180);
    public static final Color BRIGHT_TEXT_COLOR = new Color(220, 220, 220);

    public static final Font HEADING_FONT = new Font("Courier", Font.BOLD, 16);

    private int maxChannelWidth = DEFAULT_SIZE.width / 7;
    private int maxSidePanelGrids = DEFAULT_SIZE.height / SIDE_PANEL_HEIGHT;
    private int maxMessageGrids = DEFAULT_SIZE.height / MESSAGE_PANEL_HEIGHT;
    /** The index to load channels from, inclusive. */
    private int channelOffset = 0;
    /** The index to load users from, inclusive. */
    private int userOffset = 0;
    /** The index to load messages from, inclusive, to work with gridlayout. */
    private int messageOffset = 0;
    /** The index to load friends from, inclusive. */
    private int friendOffset = 0;

    private JPanel channelPanel;
    private JPanel userPanel;
    private JPanel typingPanel;
    private JPanel configPanel;
    private JPanel profileConfigPanel;
    private JPanel channelConfigPanel;
    private JPanel textPanel;
    private JPanel sideButtonPanel;

    private ChannelCreateFrame addChannelFrame;
    private ProfileFrame profileFrame;

    private JLabel profileLabel;
    private JButton sendButton, quitButton, profileButton;
    private JButton addUserButton, deleteUserButton;
    private JButton addChannelButton, deleteChannelButton;
    private JButton homeButton;
    private JButton leaveChannelButton;
    private JButton addFriendButton;
    private JLabel friendsLabel;
    private JTextField typeField;

    private JLabel channelIndicator;

    private ChatClient client;

    private boolean requestedMessages = false;

    // private ArrayList<ChannelPanel> activeChannelPanels;
    // private ArrayList<UserPanel> activeUserPanels;

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

        initializeComponents(client);

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

        // Implement channel scrolling
        channelPanel.addMouseWheelListener(new MouseWheelListener() {
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.getWheelRotation() < 0) {
                    if (channelOffset > 0) {
                        channelOffset--;
                        onlyReloadChannels();
                    }
                } else {
                    if (channelOffset - 1 < client.getChannels().size() - 2) {
                        channelOffset++;
                        onlyReloadChannels();
                    }
                }
            }
        });
        userPanel.addMouseWheelListener(new MouseWheelListener() {
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.getWheelRotation() < 0) {
                    if (userOffset > 0) {
                        userOffset--;
                        onlyReloadUsers();
                    }
                } else {
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
        });

        // TODO: make sure this is right, incorporate this into message loading
        textPanel.addMouseWheelListener(new MouseWheelListener() {
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (!client.hasCurrentChannel()) {
                    return;
                }

                if (e.getWheelRotation() > 0) {
                    if (messageOffset > 0) {
                        messageOffset--;
                        onlyReloadMessages();
                    }
                } else {
                    if (!client.hasCurrentChannel()) {
                        return;
                    }

                    Channel curChannel = client.getCurrentChannel();

                    if (curChannel.getMessages().size() > maxMessageGrids
                            && curChannel.getMessages().size() - messageOffset > maxMessageGrids) {
                        messageOffset++;
                        onlyReloadMessages();
                    } else if (curChannel.getMessages().size() >= Channel.LOCAL_SAVE_AMT
                            && (curChannel.getMessages().size() - messageOffset) <= maxMessageGrids
                            && !requestedMessages) {
                        client.offerEvent(new ClientRequestMessageEvent(client.getUser(),
                                curChannel.getMessages().get(0), client.getCurrentChannel()));
                        requestedMessages = true;
                    }
                }
            }
        });
        textPanel.addMouseWheelListener(new MouseWheelListener() {
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (client.hasCurrentChannel()) {
                    return;
                }

                if (e.getWheelRotation() < 0) {
                    if (friendOffset > 0) {
                        friendOffset--;
                        onlyReloadFriends();
                    }
                } else {
                    if (client.getFriends().size() > maxMessageGrids
                            && client.getFriends().size() - userOffset > maxSidePanelGrids) {
                        friendOffset++;
                        onlyReloadFriends();
                    }
                }
            }
        });

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
        profileLabel = ComponentFactory.createLabel(this.client.getUser().getUsername(), TEXT_COLOR);
        profileLabel.setFont(HEADING_FONT);
        profileButton = ComponentFactory.createButton("", TEXT_COLOR, PANEL_COLOR,
                new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        System.out.println("max grids: " + maxSidePanelGrids); // TODO: yeet
                        if (!hasActiveProfileFrame()) {
                            profileFrame = new ProfileFrame(client);
                            profileFrame.setVisible(true);

                            System.out.println("SYSTEM: Opened profile frame");
                        }
                    }
                });
        profileButton.setBorder(null);
        profileButton.setIcon(new ImageIcon(client.getUser().getPfp().getScaledInstance(48, 48, Image.SCALE_SMOOTH)));
        profileConfigPanel.add(profileButton);
        profileConfigPanel.add(profileLabel);

        deleteChannelButton = ComponentFactory.createImageButton("DELETE CHANNEL", "data/system/trash.png", 20, 20,
                TEXT_COLOR, PANEL_COLOR,
                new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        JLabel label = ComponentFactory.createLabel("Are you sure you want to delete this channel?",
                                BRIGHT_TEXT_COLOR);
                        ActionListener action = new ActionListener() {
                            public void actionPerformed(ActionEvent evt2) {
                                client.offerEvent(new ChannelDeleteEvent(client.getUser(), client.getCurrentChannel()));

                                System.out.println(
                                        "SYSTEM: Deleting channel " + client.getCurrentChannel().getChannelName());
                            }
                        };

                        JFrame deleteChannel = FrameFactory.createConfirmFrame("Delete channel", MAIN_COLOR, label,
                                action);
                        deleteChannel.setVisible(true);
                    }
                });

        addUserButton = ComponentFactory.createImageButton("ADD USER", "data/system/adduser.png", 20, 20, TEXT_COLOR,
                PANEL_COLOR, new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                        JLabel label = ComponentFactory.createLabel(
                                "Add a username to add, starting with @ (eg. @EmeraldPhony)",
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
        });

        deleteUserButton = ComponentFactory.createImageButton("REMOVE USER", "data/system/removeuser.png", 20, 20,
                TEXT_COLOR, PANEL_COLOR, new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                        JLabel label = ComponentFactory.createLabel(
                                "Add a username to remove, starting with @ (eg. @EmeraldPhony)",
                        BRIGHT_TEXT_COLOR);
                JTextField input = ComponentFactory.createTextBox(20);
                JButton submit = ComponentFactory.createButton("Remove User", new ActionListener() {
                    public void actionPerformed(ActionEvent evt2) {

                        if (input.getText() != "") {
                            String username = input.getText().replaceFirst("@", "");
                            User removedUser = client.getCurrentChannel().getUsers().get(username);

                            if (removedUser == null || client.getUser().equals(removedUser)) {
                                return;
                            }

                            client.offerEvent(new ChannelRemoveMemberEvent(client.getUser(), client.getCurrentChannel(),
                                    username));

                            System.out.println("SYSTEM: Removing user " + username);
                        }
                    }
                });

                JFrame removeUser = FrameFactory.createRequestFrame("Remove user", MAIN_COLOR, label, input, submit);
                removeUser.setVisible(true);
            }
        });

        sendButton = ComponentFactory.createButton("SEND", MAIN_COLOR, TEXT_COLOR, new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (client.getCurrentChannel() == null) {
                    return;
                }
                sendMessage(client);
            }
        });

        quitButton = ComponentFactory.createButton("QUIT", MAIN_COLOR, TEXT_COLOR, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                client.initiateShutdown();
                destroy();
            }
        });

        leaveChannelButton = ComponentFactory.createImageButton("LEAVE", "data/system/leave.png", 20, 20, TEXT_COLOR,
                PANEL_COLOR, new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        JLabel label = ComponentFactory.createLabel("Are you sure you want to leave this channel?",
                                BRIGHT_TEXT_COLOR);
                        ActionListener action = new ActionListener() {
                            public void actionPerformed(ActionEvent evt2) {
                                // This button will only exist if a current channel exists
                                client.offerEvent(new ChannelRemoveMemberEvent(client.getUser(),
                                        client.getCurrentChannel(), client.getUser().getUsername()));
                            }
                        };

                        JFrame removeConfirmPanel = FrameFactory.createConfirmFrame("Delete channel", MAIN_COLOR, label,
                                action);
                        removeConfirmPanel.setVisible(true);
                    }
                });

        addChannelButton = ComponentFactory.createImageButton("CREATE CHANNEL", "data/system/plus sign.png", 20, 20,
                TEXT_COLOR, PANEL_COLOR,
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (hasActiveChannelCreateFrame()) {
                            return;
                        }

                        addChannelFrame = new ChannelCreateFrame(client);
                        addChannelFrame.setVisible(true);
                    }
                });
        addChannelButton.setPreferredSize(new Dimension(maxChannelWidth, SIDE_PANEL_HEIGHT));

        homeButton = ComponentFactory.createImageButton("HOME", "data/system/home.png", 20, 20, MAIN_COLOR, PANEL_COLOR,
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        client.setCurrentChannel(null);

                switchChannelsToCurrent();
            }
        });
        homeButton.setPreferredSize(new Dimension(maxChannelWidth, SIDE_PANEL_HEIGHT));

        // INITIALIZE FRIENDS =================================================
        friendsLabel = ComponentFactory.createLabel("Friends", TEXT_COLOR);
        friendsLabel.setFont(HEADING_FONT);
        try {
            BufferedImage image = ImageIO.read(new File("data/system/user.png"));
            friendsLabel.setIcon(new ImageIcon(image.getScaledInstance(16, 16, Image.SCALE_SMOOTH)));
        } catch (IOException e) {
            System.out.println("SYSTEM: Could not load friend image!");
        }
        addFriendButton = ComponentFactory.createImageButton("ADD USER", "data/system/adduser.png", 20, 20, TEXT_COLOR,
                PANEL_COLOR, new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        JLabel label = ComponentFactory.createLabel(
                                "Add a friend's username, starting with @ (eg. @EmeraldPhony)", BRIGHT_TEXT_COLOR);
                        JTextField input = ComponentFactory.createTextBox(20);
                        JButton submit = ComponentFactory.createButton("Add Friend!", new ActionListener() {
                            public void actionPerformed(ActionEvent evt2) {
                                addFriend(input.getText());
                            }
                        });

                        JFrame addFriend = FrameFactory.createRequestFrame("Add friend", MAIN_COLOR, label, input,
                                submit);
                        addFriend.setVisible(true);
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

                    sendMessage(client);
                }
            }
        });

        channelIndicator = ComponentFactory.createLabel("No channel selected.", SECONDARY_TEXT_COLOR);
        channelIndicator.setFont(HEADING_FONT);
    }

    private void sendMessage(ChatClient client) {
        if (typeField.getText() == "") {
            return;
        }

        Message msg = new Message(typeField.getText(), client.getUser().getUsername(), -1, new Date(),
                client.getCurrentChannel());
        client.offerEvent(new MessageSentEvent(client.getUser(), msg));
        typeField.setText("");

        System.out.println("SYSTEM: Sent message " + typeField.getText());
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
        } else if (source instanceof ChannelAddMemberEvent || source instanceof ChannelRemoveMemberEvent) {
            this.reloadUsers();
        } else if (source instanceof ChannelHierarchyChangeEvent) {
            this.reloadUsers();
            this.reloadMessages();
        } else if (source instanceof ClientProfileUpdateEvent) {
            this.reloadUsers();
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
            typeField.setEditable(true);
            typeField.setText("");
            channelIndicator.setText("Channel: " + client.getCurrentChannel().getChannelName());

            channelConfigPanel.remove(friendsLabel);
            channelConfigPanel.remove(addFriendButton);

            channelConfigPanel.add(leaveChannelButton);
            channelConfigPanel.add(addUserButton);
            if (client.getCurrentChannel().getAdminUsers().contains(client.getUser())) {
                channelConfigPanel.add(deleteUserButton);
                channelConfigPanel.add(deleteChannelButton);
            }
        } else {
            typeField.setText("Messaging Disabled...");
            typeField.setEditable(false);
            channelIndicator.setText("No channel selected.");

            channelConfigPanel.remove(leaveChannelButton);
            channelConfigPanel.remove(addUserButton);
            channelConfigPanel.remove(deleteUserButton);
            channelConfigPanel.remove(deleteChannelButton);

            channelConfigPanel.add(friendsLabel);
            channelConfigPanel.add(addFriendButton);
        }

        this.reloadMessages();
        this.reloadUsers();
        this.reloadChannels();
        this.reloadFriends();

        this.repaint();
        this.revalidate();
    }

    private synchronized void reloadProfile() {
        profileButton.setIcon(new ImageIcon(client.getUser().getPfp().getScaledInstance(48, 48, Image.SCALE_SMOOTH)));
    }

    // TODO: make this a lot better
    private synchronized void reloadMessages() {
        if (client.getCurrentChannel() == null) {
            return;
        }

        textPanel.removeAll();

        ArrayList<Message> messages = client.getCurrentChannel().getMessages();

        for (int messageIndex = 0; messageIndex < messages.size(); messageIndex++) {
            if (messageIndex >= messages.size() - messageOffset
                    || messageIndex < (messages.size() - maxMessageGrids) - messageOffset) {
                continue;
            }

            Message msg = messages.get(messageIndex);
            Channel sourceChannel = client.getCurrentChannel();
            User sender = sourceChannel.getUsers().get(msg.getSenderUsername());

            boolean showAdmin = sourceChannel.getAdminUsers().contains(client.getUser());
            boolean showHeader = false;

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

    private synchronized void reloadUsers() {
        userPanel.removeAll();

        if (client.getCurrentChannel() == null) {
            return;
        }

        Channel curChannel = client.getCurrentChannel();

        Iterator<User> userIterator = client.getCurrentChannel().getUsers().values().iterator();
        for (int userIndex = 0; userIterator.hasNext(); userIndex++) {
            User u = userIterator.next();

            if (userIndex < userOffset || userIndex >= maxSidePanelGrids + userOffset) {
                continue;
            }

            userPanel.add(new UserPanel(client, u, curChannel.getAdminUsers().contains(u)));
            userPanel.setMaximumSize(new Dimension(SIDE_PANEL_HEIGHT, maxChannelWidth));
        }
    }

    private synchronized void reloadChannels() {
        channelPanel.removeAll();

        if (maxSidePanelGrids >= 1) {
            addChannelButton.setPreferredSize(new Dimension(maxChannelWidth, SIDE_PANEL_HEIGHT));
            homeButton.setPreferredSize(new Dimension(maxChannelWidth, SIDE_PANEL_HEIGHT));
            channelPanel.add(sideButtonPanel);
        }
        // if (maxSidePanelGrids >= 2) {
        // channelPanel.add(homeButton);
        // channelPanel.add(addChannelButton);
        // } else if (maxSidePanelGrids >= 1) {
        // channelPanel.add(homeButton);
        // }

        if (this.client.getChannels().size() > 0) {
            Iterator<Channel> channelIterator = this.client.getChannels().values().iterator();
            for (int channelIndex = 0; channelIterator.hasNext(); channelIndex++) {
                Channel c = channelIterator.next();

                if (channelIndex < channelOffset || channelIndex >= (maxSidePanelGrids - 1) + channelOffset) {
                    continue;
                }

                Color defaultColor = SIDE_COLOR;
                // written this way so that if the current channel is null, that's okay
                if (c.equals(this.client.getCurrentChannel())) {
                    defaultColor = MAIN_COLOR;
                }

                channelPanel.add(new ChannelPanel(this.client, c, defaultColor));
                channelPanel.setMaximumSize(new Dimension(maxChannelWidth, SIDE_PANEL_HEIGHT));
            }
        }
    }

    private synchronized void reloadFriends() {
        if (client.getCurrentChannel() != null) {
            return;
        }

        textPanel.removeAll();

        Iterator<User> friendIterator = this.client.getFriends().values().iterator();
        for (int friendIndex = 0; friendIterator.hasNext(); friendIndex++) {
            User friend = friendIterator.next();

            if (friendIndex < friendOffset || friendIndex >= maxMessageGrids + friendOffset) {
                continue;
            }

            textPanel.add(new FriendPanel(this.client, friend, textPanel.getWidth()));
        }
    }

    private void onlyReloadChannels() {
        this.reloadChannels();

        this.repaint();
        this.revalidate();
    }

    private void onlyReloadUsers() {
        this.reloadUsers();

        this.repaint();
        this.revalidate();
    }

    private void onlyReloadMessages() {
        this.reloadMessages();

        this.repaint();
        this.revalidate();
    }

    private void onlyReloadFriends() {
        this.reloadFriends();

        this.repaint();
        this.revalidate();
    }

    public boolean hasActiveChannelCreateFrame() {
        return (this.addChannelFrame != null && this.addChannelFrame.isVisible());
    }

    public boolean hasActiveProfileFrame() {
        return (this.profileFrame != null && this.profileFrame.isVisible());
    }

    public ChannelCreateFrame getChannelCreateFrame() {
        return this.addChannelFrame;
    }

    public ProfileFrame getProfileFrame() {
        return this.profileFrame;
    }

    public boolean closeChannelCreateFrame() {
        if (this.addChannelFrame == null) {
            return false;
        }

        this.addChannelFrame.destroy();
        return true;
    }

    public void destroy() {
        if (this.hasActiveChannelCreateFrame()) {
            this.addChannelFrame.destroy();
        }

        super.destroy();
    }

    public void resetRequestedMessages() {
        this.requestedMessages = false;
    }

    public void switchChannelsToCurrent() {
        this.messageOffset = 0;
        this.reload();
        this.resetRequestedMessages();
    }

    private void addUserToChannel(String user) {
        if (user.equals("") || user.equals(client.getUser().getUsername())) {
            return;
        }

        client.offerEvent(
                new ChannelAddMemberEvent(client.getUser(), client.getCurrentChannel(), user.replaceFirst("@", "")));
    }

    private void addFriend(String user) {
        if (user.equals("") || user.equals(client.getUser().getUsername())) {
            return;
        }

        client.offerEvent(new FriendAddEvent(client.getUser(), user.replaceFirst("@", "")));
    }
}
