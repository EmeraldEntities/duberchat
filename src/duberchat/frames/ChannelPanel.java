package duberchat.frames;

import java.awt.event.*;
import javax.swing.*;
import java.awt.*;

import duberchat.frames.util.DuberComponentFactory;
import duberchat.client.ChatClient;

import duberchat.chatutil.Channel;

@SuppressWarnings("serial")
public class ChannelPanel extends JPanel {
    private ChatClient client;
    private Channel channel;

    JLabel channelName;

    public ChannelPanel(ChatClient client, Channel channel) {
        super();

        this.client = client;
        this.channel = channel;

        this.channelName = DuberComponentFactory.createLabel(this.channel.getChannelName(), MainMenuFrame.TEXT_COLOR,
                MainMenuFrame.SIDE_COLOR);
        this.channelName.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));

        // Centering text
        this.setLayout(new GridBagLayout());
        this.setBackground(MainMenuFrame.SIDE_COLOR);
        this.add(this.channelName);
        this.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                client.setCurrentChannel(channel.getChannelId());
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
