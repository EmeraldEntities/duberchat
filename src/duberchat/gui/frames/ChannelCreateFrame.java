package duberchat.gui.frames;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Color;
import java.awt.Insets;

import java.util.HashSet;

import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;

import duberchat.events.ChannelCreateEvent;
import duberchat.gui.filters.TextLengthFilter;
import duberchat.gui.util.ComponentFactory;
import duberchat.client.ChatClient;

/**
 * The {@code ChannelCreateFrame} is the frame responsible for the pop-up that
 * allows the client to input a name and users to add a channel to.
 * <p>
 * Created <b> 2020-12-09 </b>
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Joseph Wang
 */
@SuppressWarnings("serial")
public class ChannelCreateFrame extends DynamicGridbagFrame {
    /** The default size of this frame. */
    public static final Dimension DEFAULT_SIZE = new Dimension(400, 500);

    /** The associated client. */
    protected ChatClient client;

    /** The main panel of this frame. */
    JPanel mainPanel;
    /** The field to type in the name of the channel. */
    JTextField nameField;
    /** The field to type in users. */
    JTextField usersField;
    /** The submit button, which attempts to create the new channel. */
    JButton submitButton;

    /** The GridBagLayout for this main panel. */
    GridBagLayout layout;
    /** The constraints for the layout. */
    GridBagConstraints constraints;

    /** Whether this frame has sent a create request already or not. */
    private boolean alreadySentRequest = false;

    /**
     * Constructs a new {@code ChannelCreateFrame}.
     * 
     * @param client the associated client.
     */
    public ChannelCreateFrame(ChatClient client) {
        super("Add a new channel");

        this.client = client;
        
        this.setResizable(false);
        this.setSize(ChannelCreateFrame.DEFAULT_SIZE);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setIconImage(new ImageIcon("data/system/logo.png").getImage());

        this.initializeComponents();

        this.getRootPane().setDefaultButton(submitButton);
        this.add(mainPanel);
    }

    /**
     * Initializes this frame's components, and adds them to the main panel.
     * <p>
     * Adding should only happen once at the initialization stage, so adding in this
     * method is okay.
     */
    private void initializeComponents() {
        mainPanel = new JPanel();
        mainPanel.setBackground(MainFrame.MAIN_COLOR);
        mainPanel.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.BLACK));

        layout = new GridBagLayout();
        constraints = new GridBagConstraints();

        mainPanel.setLayout(layout);
        mainPanel.setSize(this.getSize());

        nameField = ComponentFactory.createTextBox(20, MainFrame.BRIGHT_TEXT_COLOR, MainFrame.TEXTBOX_COLOR,
                new TextLengthFilter(50), BorderFactory.createEmptyBorder(5, 5, 5, 5));
        nameField.setMinimumSize(nameField.getPreferredSize());
        usersField = ComponentFactory.createTextBox(30, MainFrame.BRIGHT_TEXT_COLOR, MainFrame.TEXTBOX_COLOR,
                new TextLengthFilter(2000), BorderFactory.createEmptyBorder(5, 5, 5, 5));
        usersField.setMinimumSize(usersField.getPreferredSize());

        JLabel channelNameLabel = ComponentFactory.createLabel("Channel Name:");
        JLabel userLabel = ComponentFactory.createLabel("Invite users.");
        JLabel userDescriptionLabel = ComponentFactory.createLabel("Leave blank if you want to invite no one.",
                MainFrame.TEXT_COLOR);
        JLabel userSyntaxLabel = ComponentFactory.createLabel("Seperate users using a comma, and put @ before name.",
                MainFrame.TEXT_COLOR);
        JLabel userExampleLabel = ComponentFactory.createLabel("(eg. @EmeraldPhony, @PolyEntities)",
                MainFrame.TEXT_COLOR);

        submitButton = ComponentFactory.createButton("Start DuberChatting!", MainFrame.MAIN_COLOR, MainFrame.TEXT_COLOR,
                new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        attemptCreatingNewChannel();
                    }
                });

        addConstrainedComponent(channelNameLabel, mainPanel, layout, constraints, 0, 0, 1, 1,
                GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER, new Insets(0, 0, 0, 0));
        addConstrainedComponent(nameField, mainPanel, layout, constraints, 0, 1, 1, 1, GridBagConstraints.HORIZONTAL,
                GridBagConstraints.CENTER, new Insets(8, 0, 8, 0));
        addConstrainedComponent(userLabel, mainPanel, layout, constraints, 0, 2, 1, 1, GridBagConstraints.HORIZONTAL,
                GridBagConstraints.CENTER, new Insets(30, 0, 0, 0));
        addConstrainedComponent(userDescriptionLabel, mainPanel, layout, constraints, 0, 3, 1, 1,
                GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER, new Insets(4, 0, 4, 0));
        addConstrainedComponent(userSyntaxLabel, mainPanel, layout, constraints, 0, 4, 1, 1,
                GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER, new Insets(4, 0, 4, 0));
        addConstrainedComponent(userExampleLabel, mainPanel, layout, constraints, 0, 5, 1, 1,
                GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER, new Insets(4, 0, 8, 0));
        addConstrainedComponent(usersField, mainPanel, layout, constraints, 0, 6, 1, 3, GridBagConstraints.BOTH,
                GridBagConstraints.CENTER, new Insets(8, 0, 8, 0));
        addConstrainedComponent(submitButton, mainPanel, layout, constraints, 0, 9, 1, 1, GridBagConstraints.REMAINDER,
                GridBagConstraints.CENTER, new Insets(8, 0, 8, 0));
    }

    /**
     * Requests a reload for this frame.
     * <p>
     * This method should only be called if channel creation failed.
     */
    public void reload() {
        this.handleFailedEvent();
    }

    private void handleFailedEvent() {
        JLabel failedText = ComponentFactory.createLabel("Failed. Add at least one existing user!", Color.RED);

        addConstrainedComponent(failedText, mainPanel, layout, constraints, 0, 10, 1, 1, GridBagConstraints.REMAINDER,
                GridBagConstraints.CENTER, new Insets(8, 0, 8, 0));

        mainPanel.revalidate();
        mainPanel.repaint();

        alreadySentRequest = false;
    }

    /**
     * Attempts to create a new channel with the text from the name field and the
     * users as specified in the user field.
     * <p>
     * This method will not send a request to create a new channel if it has already
     * sent a request, to avoid spamming the server. This method will also not
     * request to create a new channel if there is no given name for said channel.
     */
    private void attemptCreatingNewChannel() {
        // Don't allow creation if name is nothing or a request was already sent
        if (alreadySentRequest || nameField.getText().equals("")) {
            return;
        }

        String channelName = nameField.getText();
        String[] users = usersField.getText().replace("@", "").split(", *");

        HashSet<String> usernames = new HashSet<>();

        for (String user : users) {
            usernames.add(user);
        }

        String clientUsername = client.getUser().getUsername();
        client.offerEvent(new ChannelCreateEvent(clientUsername, -1, channelName, usernames, null));

        alreadySentRequest = true;
    }
}
