package duberchat.frames.util;

import java.awt.event.*;
import javax.swing.*;
import java.awt.*;

public class DuberComponentFactory {
    private DuberComponentFactory() {
    }

    static public JButton createButton(String startingText, Color foregroundColor, Color backgroundColor,
            ActionListener onClick) {
        JButton newButton = new JButton(startingText);
        newButton.setForeground(foregroundColor);
        newButton.setBackground(backgroundColor);
        newButton.addActionListener(onClick);

        return newButton;
    }

    static public JLabel createLabel(String startingText, Color foregroundColor, Color backgroundColor) {
        JLabel newLabel = new JLabel(startingText);
        newLabel.setForeground(foregroundColor);
        newLabel.setBackground(backgroundColor);

        return newLabel;
    }
}
