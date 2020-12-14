package duberchat.gui.panels;

import java.awt.GridBagLayout;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;

import javax.swing.JPanel;
import javax.swing.BorderFactory;
import javax.swing.JLabel;

import duberchat.gui.util.ComponentFactory;
import duberchat.gui.frames.MainFrame;
import duberchat.client.ChatClient;

import duberchat.chatutil.Channel;

/**
 * The {@code ChannelPanel} is designed to act like a responsive way to display
 * and interact with various channels.
 * <p>
 * This panel is designed to be able to be constructed fast and simply. It
 * optimizes what it can, but references can be stored if the programmer wishes
 * to optimize this panel more. Measures should be taken to avoid memory leaks.
 * <p>
 * Created <b>2020-12-09</b>
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Joseph Wang
 */
@SuppressWarnings("serial")
public class ChannelPanel extends JPanel {
    /** The associated client. */
    protected ChatClient client;
    /** The channel to be represented by this panel. */
    private Channel channel;

    /** The name of the channel/ */
    JLabel channelName;

    /**
     * Initializes a new {@code ChannelPanel}. This constructor is expected to be
     * called frequently, and implements with that in mind.
     * 
     * @param client       the associated client.
     * @param channel      the channel to be represented.
     * @param defaultColor the default colour of this channel panel.
     */
    public ChannelPanel(ChatClient client, Channel channel, Color defaultColor) {
        super();

        this.channel = channel;
        this.client = client;

        this.initializeComponents(defaultColor);
    }

    /**
     * Initializes this panel's components.
     * 
     * @param defaultColor the default colour of this channel panel.
     */
    private void initializeComponents(Color defaultColor) {
        this.channelName = ComponentFactory.createLabel(this.channel.getChannelName(), MainFrame.TEXT_COLOR,
                MainFrame.SIDE_COLOR);
        this.channelName.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));

        // Centering text
        this.setLayout(new GridBagLayout());
        this.setBackground(defaultColor);
        this.add(this.channelName);

        // Add mouse listener to change colours
        this.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                client.setCurrentChannel(channel.getChannelId());

                client.getMainMenuFrame().switchChannelsToCurrent();
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
