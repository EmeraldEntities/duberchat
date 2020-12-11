package duberchat.gui.panels;

import java.awt.event.*;
import javax.swing.*;
import java.awt.*;

import duberchat.chatutil.Message;
import duberchat.client.ChatClient;
import duberchat.gui.frames.DynamicGridbagFrame;
import duberchat.gui.frames.MainFrame;
import duberchat.gui.util.ComponentFactory;

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
public class MessagePanel extends JPanel {
    /** The header font. */
    private static final Font HEADER_FONT = new Font("courier", Font.BOLD, 12);
    private boolean showHeader;
    private Message msg;
    private ChatClient client;

    private JButton editButton;
    private JButton deleteButton;

    private JLabel message;
    private JLabel header;

    private JPanel buttonPanel;
    private JPanel messagePanel;
    private JPanel headerPanel;

    private FlowLayout buttonLayout;
    private GridBagLayout layout;
    private GridBagConstraints constraints;
    private MouseAdapter mouseDetector;

    public MessagePanel(ChatClient client, Message msg, boolean showHeader, int frameWidth) {
        super();

        this.showHeader = showHeader;
        this.msg = msg;
        this.client = client;

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
        // messagePanel = new JPanel();
        // headerPanel = new JPanel();
        this.setLayout(layout);
        this.setBackground(MainFrame.MAIN_COLOR);
        this.setPreferredSize(new Dimension(frameWidth, MainFrame.MESSAGE_PANEL_HEIGHT));
        this.setMinimumSize(this.getPreferredSize());
        this.setMaximumSize(this.getPreferredSize());

        header = ComponentFactory.createLabel(msg.getSenderUsername() + "  -  " + msg.getTimestamp() + "\n",
                MainFrame.TEXT_COLOR);
        header.setFont(HEADER_FONT);
        header.setPreferredSize(new Dimension(frameWidth / 2, MainFrame.MESSAGE_PANEL_HEIGHT / 2));
        header.setMinimumSize(header.getPreferredSize());

        message = ComponentFactory.createLabel(msg.getMessage(), MainFrame.BRIGHT_TEXT_COLOR);
        header.setPreferredSize(new Dimension((frameWidth / 6) * 5, MainFrame.MESSAGE_PANEL_HEIGHT / 2));
        header.setMinimumSize(header.getPreferredSize());

        editButton = ComponentFactory.createButton("EDIT", MainFrame.TEXT_COLOR, MainFrame.MAIN_COLOR,
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        System.out.println("Edited!");
                    }
                });
        editButton.setVisible(false);
        editButton.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        deleteButton = ComponentFactory.createButton("DELETE", MainFrame.TEXT_COLOR, MainFrame.MAIN_COLOR,
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        System.out.println("Deleted!");
                    }
                });
        deleteButton.setVisible(false);
        deleteButton.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        MouseAdapter messageListener = new MessageMouseListener();

        this.addMouseListener(messageListener);

        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);

    }

    private void reload() {
        this.removeAll();

        if (this.showHeader) {
            DynamicGridbagFrame.addConstrainedComponent(header, this, layout, constraints, 0, 0, 1, 1, 0.9, 0.9,
                    GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, new Insets(0, 10, 4, 0));
        }
        DynamicGridbagFrame.addConstrainedComponent(buttonPanel, this, layout, constraints, 2, 0, 1, 2, 1.0, 1.0,
                GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, new Insets(2, 10, 2, 10));
        DynamicGridbagFrame.addConstrainedComponent(message, this, layout, constraints, 0, 1, 1, 1, 0.9, 0.9,
                GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, new Insets(0, 10, 0, 10));

        this.repaint();
        this.revalidate();
    }

    public void setShowHeader(boolean value) {
        this.showHeader = value;
        this.reload();
    }

    public boolean isShowingHeader() {
        return this.showHeader;
    }

    private final class MessageMouseListener extends MouseAdapter {
        public void mouseEntered(MouseEvent e) {
            editButton.setVisible(true);
            deleteButton.setVisible(true);
        }

        public void mouseExited(MouseEvent e) {
            if (!contains(e.getPoint())) {
                editButton.setVisible(false);
                deleteButton.setVisible(false);
            }
        }
    }
}