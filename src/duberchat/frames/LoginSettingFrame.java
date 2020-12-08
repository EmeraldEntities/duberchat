package duberchat.frames;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

import duberchat.frames.filters.LimitingRegexFilter;
import duberchat.frames.util.ComponentFactory;
import duberchat.client.ChatClient;

@SuppressWarnings("serial")
public class LoginSettingFrame extends DynamicGridbagFrame {
    public static final Dimension DEFAULT_SIZE = new Dimension(300, 400);

    ChatClient client;

    JPanel mainPanel;
    GridBagLayout layout;
    GridBagConstraints constraints;

    JTextField ipField;
    JTextField portField;
    JButton submitButton;

    JLabel savedText;

    public LoginSettingFrame(ChatClient client) {
        super("Login Settings");

        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setSize(DEFAULT_SIZE);
        this.setResizable(false);

        layout = new GridBagLayout();
        constraints = new GridBagConstraints();
        mainPanel = new JPanel();
        mainPanel.setSize(this.getSize());
        mainPanel.setBackground(MainFrame.MAIN_COLOR);
        mainPanel.setLayout(layout);

        JLabel ipLabel = ComponentFactory.createLabel("IP", MainFrame.TEXT_COLOR);
        JLabel portLabel = ComponentFactory.createLabel("PORT", MainFrame.TEXT_COLOR);
        savedText = ComponentFactory.createLabel("Saved!", Color.CYAN);

        ipField = ComponentFactory.createTextBox(20, MainFrame.BRIGHT_TEXT_COLOR, MainFrame.DARK_TEXTBOX_COLOR,
                new LimitingRegexFilter(15, "^[0-9.]+$"));
        portField = ComponentFactory.createTextBox(10, MainFrame.BRIGHT_TEXT_COLOR, MainFrame.DARK_TEXTBOX_COLOR,
                new LimitingRegexFilter(6, "^\\d+$"));

        submitButton = ComponentFactory.createButton("Save", MainFrame.MAIN_COLOR, MainFrame.TEXT_COLOR,
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        client.setIp(ipField.getText());
                        client.setPort(Integer.parseInt(portField.getText()));
                        client.saveIpSettings();

                        reload();
                    }
                });

        addConstrainedComponent(ipLabel, mainPanel, layout, constraints, 0, 0, 1, 1, GridBagConstraints.HORIZONTAL,
                GridBagConstraints.CENTER, new Insets(0, 0, 0, 0));
        addConstrainedComponent(ipField, mainPanel, layout, constraints, 0, 1, 1, 1, GridBagConstraints.HORIZONTAL,
                GridBagConstraints.CENTER, new Insets(0, 0, 0, 0));
        addConstrainedComponent(portLabel, mainPanel, layout, constraints, 0, 2, 1, 1, GridBagConstraints.HORIZONTAL,
                GridBagConstraints.CENTER, new Insets(30, 0, 0, 0));
        addConstrainedComponent(portField, mainPanel, layout, constraints, 0, 3, 1, 1, GridBagConstraints.NONE,
                GridBagConstraints.CENTER, new Insets(8, 0, 16, 0));
        addConstrainedComponent(submitButton, mainPanel, layout, constraints, 0, 4, 1, 1, GridBagConstraints.REMAINDER,
                GridBagConstraints.CENTER, new Insets(8, 0, 8, 0));

        this.add(mainPanel);
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
}
