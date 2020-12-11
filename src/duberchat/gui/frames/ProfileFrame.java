package duberchat.gui.frames;

import java.awt.event.*;
import java.io.File;
import java.util.HashSet;

import javax.swing.*;
import java.awt.*;

import duberchat.events.*;
import duberchat.client.ChatClient;
import duberchat.gui.util.ComponentFactory;
import duberchat.chatutil.Channel;
import duberchat.chatutil.User;
import duberchat.client.ChatClient;

@SuppressWarnings("serial")
public class ProfileFrame extends DynamicGridbagFrame {
    public static final Dimension DEFAULT_SIZE = new Dimension(400, 500);

    private ChatClient client;

    private JPanel mainPanel;

    private JLabel profilePicture;
    private JLabel usernameLabel;

    private GridBagLayout layout;
    private GridBagConstraints constraints;

    public ProfileFrame(ChatClient client) {
        super(client.getUser().getUsername());

        this.client = client;
        User user = client.getUser();

        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setSize(DEFAULT_SIZE);
        this.setResizable(false);
        this.setIconImage(new ImageIcon("data/system/logo.png").getImage());

        layout = new GridBagLayout();
        constraints = new GridBagConstraints();

        mainPanel = new JPanel();
        mainPanel.setSize(this.getSize());
        mainPanel.setBackground(MainFrame.MAIN_COLOR);
        mainPanel.setLayout(layout);

        profilePicture = new JLabel(new ImageIcon(user.getPfp().getScaledInstance(128, 128, Image.SCALE_SMOOTH)));
        usernameLabel = ComponentFactory.createLabel(user.getUsername(), MainFrame.BRIGHT_TEXT_COLOR);

        addConstrainedComponent(profilePicture, mainPanel, layout, constraints, 0, 0, 1, 1,
                GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER, new Insets(20, 20, 20, 20));
        addConstrainedComponent(usernameLabel, mainPanel, layout, constraints, 0, 1, 1, 1,
                GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER, new Insets(0, 0, 40, 0));

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
}
