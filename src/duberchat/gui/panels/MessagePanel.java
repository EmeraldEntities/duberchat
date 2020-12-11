package duberchat.gui.panels;

import java.awt.event.*;
import javax.swing.*;
import java.awt.*;

import duberchat.chatutil.Message;
import duberchat.chatutil.User;
import duberchat.client.ChatClient;
import duberchat.events.MessageDeleteEvent;
import duberchat.events.MessageEditEvent;
import duberchat.gui.frames.DynamicGridbagFrame;
import duberchat.gui.frames.MainFrame;
import duberchat.gui.util.ComponentFactory;
import duberchat.gui.filters.TextLengthFilter;

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
    private boolean showAdmin;
    private Message msg;
    private ChatClient client;
    private User sender;

    private JButton editButton;
    private JButton deleteButton;

    private JLabel message;
    private JLabel header;
    private JLabel picture;

    private JPanel buttonPanel;
    private JPanel messagePanel;
    private JTextField messageEditField;

    private FlowLayout buttonLayout;
    private GridBagLayout layout;
    private GridBagConstraints constraints;

    public MessagePanel(ChatClient client, User sender, Message msg, boolean showHeader, boolean showAdmin,
            int frameWidth) {
        super();

        this.showHeader = showHeader;
        this.showAdmin = showAdmin;
        this.msg = msg;
        this.sender = sender;
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
        messagePanel = new JPanel();
        messagePanel.setBackground(MainFrame.MAIN_COLOR);

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

        if (this.isShowingHeader()) {
            picture = new JLabel(new ImageIcon(sender.getPfp().getScaledInstance(32, 32, Image.SCALE_SMOOTH)));
        } else {
            picture = new JLabel("");
        }
        picture.setPreferredSize(new Dimension(32, 32));
        picture.setMinimumSize(picture.getPreferredSize());
        picture.setMaximumSize(picture.getPreferredSize());

        message = ComponentFactory.createLabel(msg.getMessage(), MainFrame.BRIGHT_TEXT_COLOR);
        message.setPreferredSize(new Dimension((frameWidth / 6) * 4, MainFrame.MESSAGE_PANEL_HEIGHT / 2));
        message.setMinimumSize(message.getPreferredSize());
        messagePanel.add(message);

        messageEditField = ComponentFactory.createTextBox(60, MainFrame.BRIGHT_TEXT_COLOR, MainFrame.SIDE_COLOR,
                new TextLengthFilter(Message.MAX_LENGTH), BorderFactory.createEmptyBorder(5, 5, 5, 5));
        messageEditField.setPreferredSize(new Dimension((frameWidth / 6) * 5, MainFrame.MESSAGE_PANEL_HEIGHT / 2));
        messageEditField.setMinimumSize(new Dimension((frameWidth / 6) * 4, MainFrame.MESSAGE_PANEL_HEIGHT / 2));
        messageEditField.setMaximumSize(messageEditField.getPreferredSize());
        messageEditField.setText(msg.getMessage());
        messageEditField.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                resetEditingMessage();
            }
        });
        messageEditField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    changeMessage();
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    resetEditingMessage();
                }
            }
        });

        message.setBorder(null);
        picture.setBorder(null);
        header.setBorder(null);

        // Show edit buttons if mouse is hovering
        MouseAdapter messageListener = new MessageMouseListener();
        this.addMouseListener(messageListener);

        editButton = ComponentFactory.createButton("EDIT", MainFrame.TEXT_COLOR, MainFrame.MAIN_COLOR,
        new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        initializeEditingMessage();
                        System.out.println("Editing!");
                    }
                });
        editButton.setVisible(false);
        editButton.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        deleteButton = ComponentFactory.createButton("DELETE", MainFrame.TEXT_COLOR, MainFrame.MAIN_COLOR,
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        deleteMessage();
                    }
                });

        deleteButton.setVisible(false);
        deleteButton.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        // buttonPanel.setPreferredSize(
        // new Dimension(editButton.getWidth() + deleteButton.getWidth() + 25,
        // MainFrame.MESSAGE_PANEL_HEIGHT));
        // buttonPanel.setMinimumSize(buttonPanel.getPreferredSize());

        // Only let people edit and delete if they are the creator of the message
        if (sender.equals(client.getUser())) {
            buttonPanel.add(editButton);
            buttonPanel.add(deleteButton);
        } else if (this.showAdmin) {
            buttonPanel.add(deleteButton);
        }
    }

    private void reload() {
        this.removeAll();

        DynamicGridbagFrame.addConstrainedComponent(picture, this, layout, constraints, 0, 0, 1, 2, 0.9, 0.9,
                GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, new Insets(0, 0, 0, 0));

        if (this.isShowingHeader()) {
            DynamicGridbagFrame.addConstrainedComponent(header, this, layout, constraints, 1, 0, 1, 1, 0.9, 0.9,
                    GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, new Insets(0, 0, -10, 0));
        }

        DynamicGridbagFrame.addConstrainedComponent(buttonPanel, this, layout, constraints, 2, 0, 1, 2, 1.0, 1.0,
                GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, new Insets(1, 0, 2, 5));
        DynamicGridbagFrame.addConstrainedComponent(messagePanel, this, layout, constraints, 1, 1, 1, 1, 0.9, 0.9,
                GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, new Insets(0, 0, 0, 0));

        this.repaint();
        this.revalidate();
    }

    public void initializeEditingMessage() {
        messagePanel.remove(message);
        messagePanel.add(messageEditField);
        messagePanel.requestFocus();

        this.reload();
    }

    private void resetEditingMessage() {
        messagePanel.remove(messageEditField);
        messagePanel.add(message);

        this.reload();
    }

    private void deleteMessage() {
        MessageDeleteEvent deleteEvent = new MessageDeleteEvent(client.getUser(), msg);
        client.offerEvent(deleteEvent);
        System.out.println("SYSTEM: Deleted!");

        // If the client didn't have main menu or a channel, this panel wouldn't exist
        // It's okay to assume these two things exist
        client.getCurrentChannel().getMessages().remove(msg);

        // Ensure this message locally disappears so that no other action happens to it.
        client.getMainMenuFrame().reload(deleteEvent);

    }

    private void changeMessage() {
        String newText = messageEditField.getText();

        if (newText.equals("")) {
            this.deleteMessage();
            return;
        }

        message.setText(newText);
        /*
         * msg is a pointer to the message in the arraylist, so editing here will edit
         * the message in the channel. Also, by editing this here, we don't need to
         * reload as this will manually change the message displayed, and when the main
         * frame is reloaded, the message will have been properly edited and this panel
         * will be generated with the correct message.
         */
        msg.setMessage(newText);

        client.offerEvent(new MessageEditEvent(client.getUser(), msg));
        System.out.println("SYSTEM: edited!");

        resetEditingMessage();
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

            setBackground(MainFrame.SIDE_COLOR);
            messagePanel.setBackground(MainFrame.SIDE_COLOR);
            buttonPanel.setBackground(MainFrame.SIDE_COLOR);
        }

        public void mouseExited(MouseEvent e) {
            if (!contains(e.getPoint())) {
                editButton.setVisible(false);
                deleteButton.setVisible(false);

                setBackground(MainFrame.MAIN_COLOR);
                messagePanel.setBackground(MainFrame.MAIN_COLOR);
                buttonPanel.setBackground(MainFrame.MAIN_COLOR);
            }
        }
    }
}