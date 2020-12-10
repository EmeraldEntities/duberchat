package duberchat.gui.panels;

import java.awt.event.*;
import javax.swing.*;
import java.awt.*;

import duberchat.gui.util.ComponentFactory;
import duberchat.gui.frames.MainFrame;
import duberchat.client.ChatClient;

import duberchat.chatutil.Channel;

@SuppressWarnings("serial")
public class ChannelPanel extends JPanel {
    private Channel channel;

    JLabel channelName;

    public ChannelPanel(ChatClient client, Channel channel, Color defaultColor) {
        super();

        this.channel = channel;

        this.channelName = ComponentFactory.createLabel(this.channel.getChannelName(), MainFrame.TEXT_COLOR,
                MainFrame.SIDE_COLOR);
        this.channelName.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));

        // Centering text
        this.setLayout(new GridBagLayout());
        this.setBackground(defaultColor);
        this.add(this.channelName);
        this.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                client.setCurrentChannel(channel.getChannelId());
                System.out.println("SYSTEM: switching to " + channel.getChannelName());

                client.getMainMenuFrame().reload();
            }

            public void mouseEntered(MouseEvent e) {
                setBackground(MainFrame.DARK_SIDE_COLOR);
            }

            public void mouseExited(MouseEvent e) {
                setBackground(defaultColor);
            }
        });
    }
}
