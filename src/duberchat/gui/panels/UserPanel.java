package duberchat.gui.panels;

import java.awt.event.*;
import javax.swing.*;
import java.awt.*;

import duberchat.gui.util.ComponentFactory;
import duberchat.gui.frames.MainFrame;
import duberchat.chatutil.User;

@SuppressWarnings("serial")
public class UserPanel extends JPanel {
    User user;
    JLabel username;

    public UserPanel(User user, boolean isAdmin) {
        this.user = user;

        Color mainColor = MainFrame.BRIGHT_TEXT_COLOR;
        if (user.getStatus() == User.OFFLINE) {
            mainColor = MainFrame.TEXT_COLOR;
        } else if (isAdmin) {
            mainColor = Color.PINK;
        }
        String text = user.getUsername() + " - " + user.getStringStatus();

        this.username = ComponentFactory.createLabel(text, mainColor, MainFrame.SIDE_COLOR);
        this.username.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));

        this.setLayout(new GridBagLayout());
        this.setBackground(MainFrame.SIDE_COLOR);
        this.add(this.username);

        this.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                System.out.println("User pressed " + user.getUsername());
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
