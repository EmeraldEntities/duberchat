package duberchat.gui.panels;

import java.awt.event.*;
import javax.swing.*;
import java.awt.*;

import duberchat.gui.util.ComponentFactory;
import duberchat.gui.frames.MainFrame;
import duberchat.gui.frames.OtherUserProfileFrame;
import duberchat.chatutil.User;
import duberchat.client.ChatClient;

/**
 * This class is designed to be a visual panel used to represent a user on the
 * side panel.
 * <p>
 * This panel is designed to refresh easily and can be expected to be
 * constructed fairly often.
 * <p>
 * Created <b>2020-12-09</b>
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Joseph Wang
 */
@SuppressWarnings("serial")
public class UserPanel extends JPanel {
    /** The associated client. */
    ChatClient client;
    /** The user this panel is representing. */
    User user;
    /** The represented user's username. */
    JLabel username;

    /**
     * Constructs a new {@code UserPanel}.
     * <p>
     * This constructor is expected to be called every reload, but can be optimized
     * by storing references to these panels. If doing so, however, it is imperative
     * that proper sync must be maintained with the user object and that any
     * references to this object be destroyed when needed for garbage collection to
     * occur.
     * 
     * @param client  the associated client.
     * @param user    the user this panel is representing.
     * @param isAdmin whether or not this user is admin.
     */
    public UserPanel(ChatClient client, User user, boolean isAdmin) {
        super();

        this.client = client;
        this.user = user;

        // Show name, colour, etc.
        Color mainColor = MainFrame.BRIGHT_TEXT_COLOR;
        if (user.getStatus() == User.OFFLINE) {
            mainColor = MainFrame.TEXT_COLOR;
        } else if (isAdmin) {
            mainColor = Color.PINK;
        }
        String text = user.getUsername() + " (" + user.getStringStatus() + ")";

        this.username = ComponentFactory.createLabel(text, mainColor, MainFrame.SIDE_COLOR);
        this.username.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));

        this.setLayout(new GridBagLayout());
        this.setBackground(MainFrame.SIDE_COLOR);
        this.add(this.username);

        // Create a profile frame if this panel is clicked, and change colours if
        // needed.
        this.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                OtherUserProfileFrame profileFrame = new OtherUserProfileFrame(client, user, e.getLocationOnScreen(),
                        isAdmin);
                profileFrame.setVisible(true);
                profileFrame.requestFocus();
            }

            public void mouseEntered(MouseEvent e) {
                setBackground(MainFrame.DARK_SIDE_COLOR);
            }

            public void mouseExited(MouseEvent e) {
                setBackground(MainFrame.SIDE_COLOR);
            }
        });
    }
}
