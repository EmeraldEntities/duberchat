package duberchat.gui.frames;

import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;

import duberchat.events.*;
import duberchat.client.ChatClient;
import duberchat.gui.filters.TextLengthFilter;
import duberchat.gui.util.ComponentFactory;
import duberchat.chatutil.Channel;
import duberchat.chatutil.User;
import duberchat.client.ChatClient;

@SuppressWarnings("serial")
public class ProfileFrame extends DynamicGridbagFrame {
    public static final Dimension DEFAULT_SIZE = new Dimension(400, 500);
    public static final String[] STATUS_CHOICES = { User.findStringStatus(User.ONLINE),
            User.findStringStatus(User.AWAY), User.findStringStatus(User.DND) };

    private ChatClient client;

    private JPanel mainPanel;

    private JLabel profilePicture;
    private JLabel usernameLabel;
    private JButton saveButton;
    private JLabel passwordLabel;
    private JTextField passwordField;
    private JLabel statusUpdateLabel;
    private JComboBox<String> statusUpdateBox;
    private JLabel succeedText;

    private BufferedImage currentPfp;
    private ImageIcon currentPfpIcon;
    private ImageIcon addPfpIcon;
    private int status;

    private GridBagLayout layout;
    private GridBagConstraints constraints;

    public ProfileFrame(ChatClient client) {
        super(client.getUser().getUsername());

        this.client = client;
        User user = client.getUser();
        this.status = user.getStatus();

        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setSize(DEFAULT_SIZE);
        this.setResizable(false);
        this.setIconImage(new ImageIcon("data/system/logo.png").getImage());

        layout = new GridBagLayout();
        constraints = new GridBagConstraints();

        mainPanel = new JPanel();
        mainPanel.setSize(this.getSize());
        mainPanel.setBackground(MainFrame.PANEL_COLOR);
        mainPanel.setLayout(layout);

        try {
            BufferedImage currentPfp = user.getPfp();
            profilePicture = ComponentFactory
                    .createImageLabel(currentPfp.getScaledInstance(128, 128, Image.SCALE_SMOOTH));

            currentPfpIcon = new ImageIcon(currentPfp.getScaledInstance(128, 128, Image.SCALE_SMOOTH));
            addPfpIcon = new ImageIcon(ImageIO.read(new File("data/system/plus sign.png")).getScaledInstance(128, 128,
                    Image.SCALE_SMOOTH));

        } catch (IOException e) {
            System.out.println("SYSTEM: Failed to load profile picture.");
        }

        profilePicture.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                JFileChooser fc = new JFileChooser();
                fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                int option = fc.showOpenDialog(ProfileFrame.this);

                if (option == JFileChooser.APPROVE_OPTION) {
                    try {
                        currentPfp = ImageIO.read(fc.getSelectedFile().getAbsoluteFile());
                        currentPfpIcon = new ImageIcon(currentPfp.getScaledInstance(128, 128, Image.SCALE_SMOOTH));
                    } catch (IOException e2) {
                        System.out.println("SYSTEM: Failed to load new profile picture");
                    }

                    reload();
                }
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
                        User newUser = new User(client.getUser());
                        newUser.setPfp(currentPfp);
                        newUser.setStatus(status);
                        if (!passwordField.getText().equals("")) {
                            newUser.setHashedPassword(passwordField.getText().hashCode());
                        }

                        client.offerEvent(new ClientProfileUpdateEvent(newUser));
                    }
                });
        passwordLabel = ComponentFactory.createLabel("Change Password", MainFrame.TEXT_COLOR);
        passwordField = ComponentFactory.createPasswordBox(20, MainFrame.BRIGHT_TEXT_COLOR, MainFrame.SIDE_COLOR,
                new TextLengthFilter(40), BorderFactory.createEmptyBorder(5, 5, 5, 5));
        passwordField.setText("");

        statusUpdateLabel = ComponentFactory.createLabel("Update Status", MainFrame.TEXT_COLOR);
        statusUpdateBox = ComponentFactory.createComboBox(STATUS_CHOICES, status - 1, MainFrame.BRIGHT_TEXT_COLOR,
                MainFrame.SIDE_COLOR, new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        int statusConstant = User.findIntStatus((String) statusUpdateBox.getSelectedItem());
                        if (statusConstant != -1) {
                            status = statusConstant;

                            String newUsernameText = user.getUsername() + " ("+ User.findStringStatus(status) + ")";
                            usernameLabel.setText(newUsernameText);
                            reload();
                        }
                    }
                });
        succeedText = ComponentFactory.createLabel("Saved!", Color.CYAN);

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

        this.add(mainPanel);
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
}
