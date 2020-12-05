package duberchat.frames;

import java.awt.event.*;
import javax.swing.*;
import java.awt.*;

import java.util.EventObject;
import java.util.concurrent.ConcurrentLinkedQueue;

import duberchat.events.*;

@SuppressWarnings("serial")
public class MainMenuFrame extends JFrame {
    public MainMenuFrame(String title) {
        super(title);

        this.setSize(Toolkit.getDefaultToolkit().getScreenSize());
    }
}
