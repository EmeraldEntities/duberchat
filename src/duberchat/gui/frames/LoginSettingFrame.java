package duberchat.gui.frames;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Color;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;

import duberchat.client.ChatClient;
import duberchat.gui.filters.LimitingRegexFilter;
import duberchat.gui.util.ComponentFactory;

/**
 * This class is designed to serve as a general settings frame for the login
 * page.
 * <p>
 * This allows the user to save a custom designated IP and port so that they do
 * not need to type in either each time they log in.
 * <p>
 * IP configurations are saved to the file {@code data/ipconfig}.
 * <p>
 * Created <b>2020-12-09</b>
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Joseph Wang
 */
@SuppressWarnings("serial")
public class LoginSettingFrame extends DynamicGridbagFrame {
    /** The default size for this frame. */
    public static final Dimension DEFAULT_SIZE = new Dimension(300, 400);

    /** The associated client. */
    protected ChatClient client;

    /** The main panel of this frame. */
    private JPanel mainPanel;
    /** The GridBagLayout for this frame. */
    private GridBagLayout layout;
    /** A shared constraints object for working with the layout. */
    private GridBagConstraints constraints;

    /** The text field for the IP. */
    private JTextField ipField;
    /** The text field for the port number. */
    private JTextField portField;
    /** The save button which saves the ip configs. */
    private JButton saveButton;
    /** The label that labels the ip field. */
    private JLabel ipLabel;
    /** The label that labels the port field. */
    private JLabel portLabel;
    /** The text that appears upon save. */
    private JLabel savedText;

    /**
     * Constructs a new {@code LoginSettingFrame}.
     * 
     * @param client the associated client.
     */
    public LoginSettingFrame(ChatClient client) {
        super("Login Settings");

        this.client = client;

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
        mainPanel.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.BLACK));

        this.initializeComponents();
        this.addComponents();
        this.getRootPane().setDefaultButton(saveButton);

        this.add(mainPanel);
    }

    /**
     * Initializes all the required components for this frame.
     */
    private void initializeComponents() {
        ipLabel = ComponentFactory.createLabel("IP", MainFrame.TEXT_COLOR);
        portLabel = ComponentFactory.createLabel("PORT", MainFrame.TEXT_COLOR);
        savedText = ComponentFactory.createLabel("Saved!", Color.CYAN);

        // Create the text boxes
        ipField = ComponentFactory.createTextBox(20, MainFrame.BRIGHT_TEXT_COLOR, MainFrame.DARK_TEXTBOX_COLOR,
                new LimitingRegexFilter(15, "^[0-9.]+$"), BorderFactory.createEmptyBorder(5, 5, 5, 5));
        ipField.setText(client.getIp());

        portField = ComponentFactory.createTextBox(10, MainFrame.BRIGHT_TEXT_COLOR, MainFrame.DARK_TEXTBOX_COLOR,
                new LimitingRegexFilter(6, "^\\d+$"), BorderFactory.createEmptyBorder(5, 5, 5, 5));
        portField.setText(Integer.toString(client.getPort()));

        saveButton = ComponentFactory.createButton("Save", MainFrame.MAIN_COLOR, MainFrame.TEXT_COLOR,
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        saveIpSettings();
                    }
                });
    }

    /**
     * Adds all the required components to the main panel of this frame.
     */
    private void addComponents() {
        addConstrainedComponent(ipLabel, mainPanel, layout, constraints, 0, 0, 1, 1, GridBagConstraints.HORIZONTAL,
                GridBagConstraints.CENTER, new Insets(0, 0, 8, 0));
        addConstrainedComponent(ipField, mainPanel, layout, constraints, 0, 1, 1, 1, GridBagConstraints.HORIZONTAL,
                GridBagConstraints.CENTER, new Insets(0, 0, 0, 0));
        addConstrainedComponent(portLabel, mainPanel, layout, constraints, 0, 2, 1, 1, GridBagConstraints.HORIZONTAL,
                GridBagConstraints.CENTER, new Insets(30, 0, 0, 0));
        addConstrainedComponent(portField, mainPanel, layout, constraints, 0, 3, 1, 1, GridBagConstraints.NONE,
                GridBagConstraints.CENTER, new Insets(8, 0, 16, 0));
        addConstrainedComponent(saveButton, mainPanel, layout, constraints, 0, 4, 1, 1, GridBagConstraints.REMAINDER,
                GridBagConstraints.CENTER, new Insets(8, 0, 8, 0));
    }

    /**
     * {@inheritDoc}
     * <p>
     * This should only reload on save.
     */
    public void reload() {
        addConstrainedComponent(savedText, mainPanel, layout, constraints, 0, 5, 1, 1, GridBagConstraints.NONE,
                GridBagConstraints.CENTER, new Insets(8, 0, 8, 0));

        this.repaint();
        this.revalidate();
    }

    /**
     * Attempts to save the specified IP settings, if both ip and port are present.
     * <p>
     * Also reloads this component upon save.
     */
    private void saveIpSettings() {
        if (ipField.getText().equals("") || portField.getText().equals("")) {
            return;
        }

        client.setIp(ipField.getText());
        client.setPort(Integer.parseInt(portField.getText()));
        client.saveIpSettings();

        reload();
    }
}
