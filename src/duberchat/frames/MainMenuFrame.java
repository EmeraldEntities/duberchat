package duberchat.frames;

import java.awt.event.*;
import javax.swing.*;
import java.awt.*;

import java.util.EventObject;
import java.util.concurrent.ConcurrentLinkedQueue;

import duberchat.events.*;

@SuppressWarnings("serial")
public class MainMenuFrame extends JFrame {
    JPanel channelPanel;
    JPanel userPanel; // the right one if applicable
    JPanel typingPanel;
    JPanel textPanel;

    public MainMenuFrame(String title) {
        super(title);
        this.setSize(Toolkit.getDefaultToolkit().getScreenSize());

    }
}
