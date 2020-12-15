package duberchat.gui.panels;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;

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
 * This {@code MessagePanel} is intended to be a lightweight component designed
 * to represent a message sent by a user and provides additional functionality
 * for messages, that which surpasses what can be accomplished with a TextArea
 * alone.
 * <p>
 * This panel is designed to be able to be constructed fast and simply. It
 * optimizes what it can.
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
    /** Whether or not this panel should show a header. */
    private boolean showHeader;
    /** Whether or not this panel should show admin options. */
    private boolean showAdmin;
    /** The associated message object. */
    private Message msg;
    /** The associated client. */
    protected ChatClient client;
    /** The sender of this message. */
    private User sender;

    /** The edit button for editing a message. */
    private JButton editButton;
    /** The delete button for deleting a message. */
    private JButton deleteButton;

    /** The message to be displayed. */
    private JLabel message;
    /** The header text, if required. */
    private JLabel header;
    /** The sender's picture, if required. */
    private JLabel picture;

    /** The panel that holds the buttons. */
    private JPanel buttonPanel;
    /** The panel that holds the message and the messaging editing text field. */
    private JPanel messagePanel;
    /** The message editing text field. */
    private JTextField messageEditField;

    /** The layout for the buttons. */
    private FlowLayout buttonLayout;
    /** The GridBagLayout for this frame. */
    private GridBagLayout layout;
    /** A shared constraints object for working with the layout. */
    private GridBagConstraints constraints;

    /**
     * Constructs a new {@code MessagePanel}. This panel is expected to be
     * constructed multiple times, and keeps that in mind with implementation.
     * 
     * @param client     the associated client.
     * @param sender     the sender user.
     * @param msg        the message that this panel represents.
     * @param showHeader whether this panel should show a header or not.
     * @param showAdmin  whether this panel should show admin features or not (if
     *                   the client is admin).
     * @param frameWidth the width of this frame.
     */
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

    /**
     * Initializes this panel's components.
     */
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

        // Create picture based on header
        if (this.isShowingHeader()) {
            picture = ComponentFactory.createImageLabel(sender.getPfp().getScaledInstance(32, 32, Image.SCALE_SMOOTH));
        } else {
            picture = ComponentFactory.createLabel("");
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

        // Only let people edit and delete if they are the creator of the message
        if (sender.equals(client.getUser())) {
            buttonPanel.add(editButton);
            buttonPanel.add(deleteButton);
        } else if (this.showAdmin) {
            buttonPanel.add(deleteButton);
        }
    }

    /**
     * Does a reload on this frame, and readds components.
     */
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

    /**
     * Initializes editing this message panel by adding the message edit textbox.
     */
    public void initializeEditingMessage() {
        messagePanel.remove(message);
        messagePanel.add(messageEditField);
        messagePanel.requestFocus();

        this.reload();
    }

    /**
     * Resets the editing message panel, removes the textbox, re-adds the message,
     * and discards changes.
     */
    private void resetEditingMessage() {
        messagePanel.remove(messageEditField);
        messagePanel.add(message);

        this.reload();
    }

    /**
     * Deletes this message, and sends an event to server.
     */
    private void deleteMessage() {
        String clientUsername = client.getUser().getUsername();
        MessageDeleteEvent deleteEvent = new MessageDeleteEvent(clientUsername, msg);
        client.offerEvent(deleteEvent);

        // If the client didn't have main menu or a channel, this panel wouldn't exist
        // It's okay to assume these two things exist
        client.getCurrentChannel().getMessages().remove(msg);

        // Ensure this message locally disappears so that no other action happens to it.
        client.getMainMenuFrame().reload(deleteEvent);

    }

    /**
     * Changes the message, and sends an event to the server.
     */
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

        String clientUsername = client.getUser().getUsername();
        // Make sure we're sending the new message...
        Message newMessage = new Message(msg);
        newMessage.setMessage(newText);

        client.offerEvent(new MessageEditEvent(clientUsername, newMessage));

        resetEditingMessage();
    }

    /**
     * Sets whether this message should show header or not.
     * 
     * @param value whether this message should show header or not.
     */
    public void setShowHeader(boolean value) {
        this.showHeader = value;
        this.reload();
    }

    /**
     * Checks if this message is currently showing a header.
     * 
     * @return true if this message is showing a header.
     */
    public boolean isShowingHeader() {
        return this.showHeader;
    }

    /**
     * This class is a helper inner class that listens for mouse movement inside a
     * message, and performs actions as a response.
     * <p>
     * This class is responsible for changing colours and adding buttons if a mouse
     * hovers over this panel.
     * <p>
     * 
     * Created <b>2020-12-10</b>
     * 
     * @since 1.0.0
     * @version 1.0.0
     * @author Joseph Wang
     */
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