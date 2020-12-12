package duberchat.gui.frames;

import java.awt.event.*;
import javax.swing.*;
import java.awt.*;

import duberchat.events.*;
import duberchat.client.ChatClient;
import duberchat.gui.util.ComponentFactory;
import duberchat.chatutil.Channel;
import duberchat.chatutil.User;
import duberchat.client.ChatClient;

/**
 * [INSERT DESCRIPTION HERE]
 * <p>
 * Created <b>2020-12-10</b>
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Joseph Wang
 */
@SuppressWarnings("serial")
public class OtherUserProfileFrame extends DynamicGridbagFrame {
    public static final Dimension DEFAULT_SIZE = new Dimension(300, 400);

    private ChatClient client;
    private User otherUser;

    private JPanel mainPanel;

    private JLabel profilePicture;
    private JLabel usernameLabel;
    private JButton dmButton;

    private GridBagLayout layout;
    private GridBagConstraints constraints;

    private boolean sentDmRequest = false;

    public OtherUserProfileFrame(ChatClient client, User otherUser, Point clickLocation) {
        super(otherUser.getUsername());

        this.client = client;
        this.otherUser = otherUser;

        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setSize(DEFAULT_SIZE);
        this.setResizable(false);
        this.setUndecorated(true);
        this.setIconImage(new ImageIcon("data/system/logo.png").getImage());

        layout = new GridBagLayout();
        constraints = new GridBagConstraints();

        mainPanel = new JPanel();
        mainPanel.setSize(this.getSize());
        mainPanel.setBackground(MainFrame.PANEL_COLOR);
        mainPanel.setLayout(layout);

        this.setLocation(clickLocation.x - this.getWidth(), clickLocation.y);

        profilePicture = new JLabel(new ImageIcon(otherUser.getPfp().getScaledInstance(128, 128, Image.SCALE_SMOOTH)));
        usernameLabel = ComponentFactory.createLabel(otherUser.getUsername(), MainFrame.BRIGHT_TEXT_COLOR);
        dmButton = ComponentFactory.createButton("Duberchat!", MainFrame.PANEL_COLOR, MainFrame.TEXT_COLOR,
                new CreateChannelActionListener());

        addConstrainedComponent(profilePicture, mainPanel, layout, constraints, 0, 0, 1, 1,
                GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER, new Insets(20, 20, 20, 20));
        addConstrainedComponent(usernameLabel, mainPanel, layout, constraints, 0, 1, 1, 1,
                GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER, new Insets(0, 0, 40, 0));
        addConstrainedComponent(dmButton, mainPanel, layout, constraints, 0, 2, 1, 1, GridBagConstraints.REMAINDER,
                GridBagConstraints.CENTER, new Insets(0, 0, 0, 0));

        // this.addFocusListener(new FocusAdapter() {
        // public void focusLost(FocusEvent e) {
        // reload();
        // }
        // });
        this.addWindowListener(new WindowAdapter() {
            public void windowDeactivated(WindowEvent e) {
                reload();
            }
        });

        this.add(mainPanel);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Reloading this frame destroys this frame.
     */
    public void reload() {
        this.destroy();
    }

    private class CreateChannelActionListener implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            if (sentDmRequest || otherUser.equals(client.getUser())) {
                return;
            }

            Channel newChannel = new Channel("@" + otherUser.getUsername());
            client.offerEvent(new ChannelCreateEvent(client.getUser(), newChannel, otherUser.getUsername()));

            sentDmRequest = true;
            System.out.println("SYSTEM: Created DM with " + otherUser.getUsername() + ".");
        }
    }
}

//