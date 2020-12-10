package duberchat.gui.frames;

import java.awt.event.*;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Date;

import duberchat.events.*;
import duberchat.gui.filters.TextLengthFilter;
import duberchat.gui.util.ComponentFactory;
import duberchat.gui.util.FrameFactory;
import duberchat.gui.panels.ChannelPanel;
import duberchat.gui.panels.UserPanel;
import duberchat.client.ChatClient;

import duberchat.chatutil.Channel;
import duberchat.chatutil.Message;
import duberchat.chatutil.User;

@SuppressWarnings("serial")
public class MainFrame extends DynamicFrame {
    /**
     *
     */
    private static final int CHANNEL_SIZE = 50;
    public static final Dimension DEFAULT_SIZE = Toolkit.getDefaultToolkit().getScreenSize();
    public static final Color MAIN_COLOR = new Color(60, 60, 60);
    public static final Color SIDE_COLOR = new Color(40, 40, 40);
    public static final Color DARK_SIDE_COLOR = new Color(20, 20, 20);
    public static final Color DARK_TEXTBOX_COLOR = new Color(40, 40, 40);
    public static final Color TEXTBOX_COLOR = new Color(80, 80, 80);
    public static final Color TEXT_COLOR = new Color(150, 150, 150);
    public static final Color SECONDARY_TEXT_COLOR = new Color(180, 180, 180);
    public static final Color BRIGHT_TEXT_COLOR = new Color(220, 220, 220);
    
    private int maxChannelWidth = DEFAULT_SIZE.width / 4;
    private int maxGrids = DEFAULT_SIZE.height / CHANNEL_SIZE;

    private JPanel channelPanel;
    private JPanel userPanel;
    private JPanel typingPanel;
    private JPanel configPanel;
    private JPanel profileConfigPanel;
    private JPanel channelConfigPanel;
    private JPanel textPanel;

    private ChannelCreateFrame addChannelFrame;

    private JButton sendButton, quitButton, profileButton;
    private JButton addUserButton, deleteUserButton;
    private JButton addChannelButton, deleteChannelButton;
    private JButton homeButton;
    private JTextField typeField;
    private JTextArea msgArea;

    private JLabel channelIndicator;

    private ConcurrentLinkedQueue<SerializableEvent> output;
    private ChatClient client;

    // private ArrayList<ChannelPanel> activeChannelPanels;
    // private ArrayList<UserPanel> activeUserPanels;

    public MainFrame(String title, ChatClient client, ConcurrentLinkedQueue<SerializableEvent> outgoingEvents) {
        super(title);
        this.client = client;
        this.output = outgoingEvents;

        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
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
                reinitializeLayouts();
            }
        });

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

        // typingPanel.setLayout(new GridLayout(2, 0));
        GridBagLayout typingPanelLayout = new GridBagLayout();
        typingPanel.setLayout(typingPanelLayout);
        configPanel.setLayout(new GridLayout(1, 2));
        channelPanel.setLayout(new GridLayout(this.getHeight() / CHANNEL_SIZE, 1));
        userPanel.setLayout(new GridLayout(this.getHeight() / CHANNEL_SIZE, 1));
        profileConfigPanel.setLayout(new FlowLayout(FlowLayout.LEADING, 25, 5));
        channelConfigPanel.setLayout(new FlowLayout(FlowLayout.TRAILING, 25, 5));
        configPanel.add(profileConfigPanel);
        configPanel.add(channelConfigPanel);

        channelPanel.addMouseWheelListener(new MouseWheelListener() {
            public void mouseWheelMoved(MouseWheelEvent e) {
                System.out.println(e.getWheelRotation());
            }
        });

        // Initialize defaults

        GridBagConstraints gbc = new GridBagConstraints();
        DynamicGridbagFrame.addConstrainedComponent(typeField, typingPanel, typingPanelLayout, gbc, 0, 0, 5, 1, 1.0,
                1.0, GridBagConstraints.BOTH, GridBagConstraints.CENTER, new Insets(0, 20, 0, 20));
        DynamicGridbagFrame.addConstrainedComponent(sendButton, typingPanel, typingPanelLayout, gbc, 5, 0, 1, 1, 0.5,
                1.0, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER, new Insets(0, 20, 0, 20));
        DynamicGridbagFrame.addConstrainedComponent(channelIndicator, typingPanel, typingPanelLayout, gbc, 0, 1, 5, 1,
                1.0, 1.0, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER, new Insets(0, 20, 0, 20));
        DynamicGridbagFrame.addConstrainedComponent(quitButton, typingPanel, typingPanelLayout, gbc, 5, 1, 1, 1, 0.5,
                1.0, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER, new Insets(0, 20, 0, 20));

        this.add(BorderLayout.WEST, channelPanel);
        this.add(BorderLayout.EAST, userPanel);
        this.add(BorderLayout.NORTH, configPanel);
        this.add(BorderLayout.CENTER, msgArea);
        this.add(BorderLayout.SOUTH, typingPanel);

        this.reload();
    }

    private void reinitializeLayouts() {
        channelPanel.removeAll();
        userPanel.removeAll();

        this.maxGrids = this.getHeight() / CHANNEL_SIZE;
        this.maxChannelWidth = this.getWidth() / 4;

        channelPanel.setLayout(new GridLayout(this.maxGrids, 1));
        userPanel.setLayout(new GridLayout(this.maxGrids, 1));

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

        channelPanel.setBackground(SIDE_COLOR);
        userPanel.setBackground(SIDE_COLOR);
        typingPanel.setBackground(SIDE_COLOR);
        profileConfigPanel.setBackground(DARK_SIDE_COLOR);
        channelConfigPanel.setBackground(DARK_SIDE_COLOR);

        // INITIALIZE BUTTONS ====================================================
        profileButton = ComponentFactory.createButton(client.getUser().getUsername(), MAIN_COLOR, TEXT_COLOR,
                new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        System.out.println("You pressed the profile button!");
                    }
                });
        profileConfigPanel.add(profileButton);

        deleteChannelButton = ComponentFactory.createButton("DELETE CHANNEL", MAIN_COLOR, TEXT_COLOR,
                new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        JLabel label = ComponentFactory.createLabel("Are you sure you want to delete this channel?",
                                BRIGHT_TEXT_COLOR);
                        ActionListener action = new ActionListener() {
                            public void actionPerformed(ActionEvent evt2) {
                                output.offer(new ChannelDeleteEvent(client.getUser(), client.getCurrentChannel()));

                                System.out.println(
                                        "SYSTEM: Deleting channel " + client.getCurrentChannel().getChannelName());
                            }
                        };

                        JFrame deleteChannel = FrameFactory.createConfirmFrame("Delete channel", MAIN_COLOR, label,
                                action);
                        deleteChannel.setVisible(true);
                    }
                });

        addUserButton = ComponentFactory.createButton("ADD USER", MAIN_COLOR, TEXT_COLOR, new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                JLabel label = ComponentFactory.createLabel("Add a username, starting with @ (eg. @EmeraldPhony)",
                        BRIGHT_TEXT_COLOR);
                JTextField input = ComponentFactory.createTextBox(20);
                JButton submit = ComponentFactory.createButton("Add User", new ActionListener() {
                    public void actionPerformed(ActionEvent evt2) {

                        if (input.getText() != "") {
                            output.offer(new ChannelAddMemberEvent(client.getUser(), client.getCurrentChannel(),
                                    input.getText().replaceFirst("@", "")));

                            System.out.println("SYSTEM: Adding user " + input.getText().replaceFirst("@", ""));
                        }
                    }
                });

                JFrame addUser = FrameFactory.createRequestFrame("Add user", MAIN_COLOR, label, input, submit);
                addUser.setVisible(true);
            }
        });

        deleteUserButton = ComponentFactory.createButton("REMOVE USER", MAIN_COLOR, TEXT_COLOR, new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                JLabel label = ComponentFactory.createLabel("Add a username, starting with @ (eg. @EmeraldPhony)",
                        BRIGHT_TEXT_COLOR);
                JTextField input = ComponentFactory.createTextBox(20);
                JButton submit = ComponentFactory.createButton("Remove User", new ActionListener() {
                    public void actionPerformed(ActionEvent evt2) {

                        if (input.getText() != "") {
                            String username = input.getText().replaceFirst("@", "");
                            // Create a temp new user to check if they exist
                            User tempUser = new User(username);
                            if (!client.getCurrentChannel().getUsers().contains(tempUser))
                                return;

                            output.offer(new ChannelRemoveMemberEvent(client.getUser(), client.getCurrentChannel(),
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
                client.logout();
                destroy();
            }
        });

        addChannelButton = ComponentFactory.createButton("CREATE CHANNEL", MAIN_COLOR, TEXT_COLOR,
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (hasActiveChannelCreateFrame()) {
                            return;
                        }

                        addChannelFrame = new ChannelCreateFrame(client, output);
                        addChannelFrame.setVisible(true);
                    }
                });

        homeButton = ComponentFactory.createButton("HOME", MAIN_COLOR, TEXT_COLOR, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                client.setCurrentChannel(null);

                reload();
            }
        });

        // INITIALIZE TYPING AREA =================================================
        typeField = ComponentFactory.createTextBox(20, BRIGHT_TEXT_COLOR, TEXTBOX_COLOR, new TextLengthFilter(100));
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

        msgArea = new JTextArea();
        msgArea.setEditable(false);
        msgArea.setBackground(MAIN_COLOR);
        msgArea.setForeground(BRIGHT_TEXT_COLOR);
        msgArea.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        channelIndicator = ComponentFactory.createLabel("No channel selected.", SECONDARY_TEXT_COLOR);
        channelIndicator.setFont(new Font("Courier", Font.BOLD, 16));
    }

    private void sendMessage(ChatClient client) {
        if (typeField.getText() == "") {
            return;
        }

        Message msg = new Message(typeField.getText(), client.getUser().getUsername(), -1, new Date(),
                client.getCurrentChannel());
        output.offer(new MessageSentEvent(client.getUser(), msg));
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
    public void reload(SerializableEvent source) {
        if (source instanceof MessageEvent) {
            this.reloadMessages();
        } else if (source instanceof ClientEvent) {
            this.reloadUsers();
        } else if (source instanceof ChannelAddMemberEvent || source instanceof ChannelRemoveMemberEvent) {
            this.reloadUsers();
        } else if (source instanceof ChannelEvent) {
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
    public void reload() {
        if (client.getCurrentChannel() != null) {
            typeField.setEditable(true);
            typeField.setText("");
            channelIndicator.setText("Channel: " + client.getCurrentChannel().getChannelName());

            channelConfigPanel.add(addUserButton);
            if (client.getCurrentChannel().getAdminUsers().contains(client.getUser())) {
                channelConfigPanel.add(deleteUserButton);
                channelConfigPanel.add(deleteChannelButton);
            }
        } else {
            typeField.setText("Messaging Disabled...");
            typeField.setEditable(false);
            channelIndicator.setText("No channel selected.");

            channelConfigPanel.remove(addUserButton);
            channelConfigPanel.remove(deleteUserButton);
            channelConfigPanel.remove(deleteChannelButton);
        }

        this.reloadMessages();
        this.reloadUsers();
        this.reloadChannels();

        this.repaint();
        this.revalidate();
    }

    // TODO: make this a lot better
    private void reloadMessages() {
        msgArea.setText("");

        if (client.getCurrentChannel() == null) {
            return;
        }

        ArrayList<Message> messages = client.getCurrentChannel().getMessages();

        for (int i = messages.size() - 1; i >= 0; i--) {
            Message msg = messages.get(i);
            if (i != messages.size() - 1) {
                if (!(msg.getSenderUsername().equals(messages.get(i + 1).getSenderUsername()))) {
                    msgArea.append(msg.getSenderUsername() + "  -  " + msg.getTimestamp() + "\n");
                }
            } else {
                msgArea.append(msg.getSenderUsername() + "  -  " + msg.getTimestamp() + "\n");
            }

            msgArea.append(msg.getMessage() + "\n");
        }

        // for (int i = 0; i < messages.size(); i++) {
        // Message msg = messages.get(i);
        // if (i != 0) {
        // if (!(msg.getSenderUsername().equals(messages.get(i -
        // 1).getSenderUsername()))) {
        // msgArea.append(msg.getSenderUsername() + " - " + msg.getTimestamp() + "\n");
        // }
        // } else {
        // msgArea.append(msg.getSenderUsername() + " - " + msg.getTimestamp() + "\n");
        // }

        // msgArea.append(msg.getMessage() + "\n");
        // }
    }

    // TODO: implement
    private void reloadUsers() {
        userPanel.removeAll();

        if (client.getCurrentChannel() == null) {
            return;
        }

        Channel curChannel = client.getCurrentChannel();
        for (User u : curChannel.getUsers()) {
            System.out.println(u.getUsername());
            userPanel.add(new UserPanel(u, curChannel.getAdminUsers().contains(u)));
            userPanel.setMaximumSize(new Dimension(CHANNEL_SIZE, maxChannelWidth));
        }
    }

    private void reloadChannels() {
        channelPanel.removeAll();
        channelPanel.add(homeButton);
        channelPanel.add(addChannelButton);

        if (this.client.getChannels().size() > 0) {
            for (Channel c : this.client.getChannels().values()) {
                System.out.println(c.getChannelName());

                Color defaultColor = SIDE_COLOR;
                // written this way so that if the current channel is null, that's okay
                if (c.equals(this.client.getCurrentChannel())) {
                    defaultColor = MAIN_COLOR;
                }

                channelPanel.add(new ChannelPanel(this.client, c, defaultColor));
                channelPanel.setMaximumSize(new Dimension(CHANNEL_SIZE, maxChannelWidth));
            }
        }
    }

    public boolean hasActiveChannelCreateFrame() {
        return (this.addChannelFrame != null && this.addChannelFrame.isVisible());
    }

    public ChannelCreateFrame getChannelCreateFrame() {
        return this.addChannelFrame;
    }

    public boolean closeChannelCreateFrame() {
        if (this.addChannelFrame == null) {
            return false;
        }

        this.addChannelFrame.destroy();
        return true;
    }
}
