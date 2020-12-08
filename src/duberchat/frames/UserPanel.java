package duberchat.frames;

import java.awt.event.*;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Date;

import duberchat.events.*;
import duberchat.client.ChatClient;

import duberchat.frames.util.DuberComponentFactory;
import duberchat.chatutil.Channel;
import duberchat.chatutil.Message;
import duberchat.chatutil.User;

@SuppressWarnings("serial")
public class UserPanel extends JPanel {
    User user;

    JLabel username;

    public UserPanel(User user, boolean isAdmin) {
        this.user = user;

        Color mainColor = MainMenuFrame.TEXT_COLOR;
        if (isAdmin) {
            mainColor = Color.PINK;
        }
        String text = user.getUsername() + " - " + user.getStringStatus();

        this.username = DuberComponentFactory.createLabel(text, mainColor, MainMenuFrame.SIDE_COLOR);
        this.username.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));

        this.setLayout(new GridBagLayout());
        this.setBackground(MainMenuFrame.SIDE_COLOR);
        this.add(this.username);

        this.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                System.out.println("User pressed " + user.getUsername());
            }

            public void mouseEntered(MouseEvent e) {
                setBackground(MainMenuFrame.DARK_SIDE_COLOR);
            }

            public void mouseExited(MouseEvent e) {
                setBackground(MainMenuFrame.SIDE_COLOR);
            }
        });
    }
}
