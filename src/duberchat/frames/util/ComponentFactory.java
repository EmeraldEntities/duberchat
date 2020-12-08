package duberchat.frames.util;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.*;

import java.awt.*;

public class ComponentFactory {
    public static Font buttonFont = new Font("Courier", Font.PLAIN, 12);

    private ComponentFactory() {
    }

    static public JButton createButton(String startingText, Color foregroundColor, Color backgroundColor,
            ActionListener onClick) {
        JButton newButton = new JButton(startingText);
        newButton.setForeground(foregroundColor);
        newButton.setBackground(backgroundColor);
        newButton.setFont(buttonFont);

        if (onClick != null) {
            newButton.addActionListener(onClick);
        }

        return newButton;
    }

    static public JButton createButton(String startingText, Color foregroundColor, Color backgroundColor) {
        return ComponentFactory.createButton(startingText, foregroundColor, backgroundColor, null);
    }

    static public JLabel createLabel(String startingText, Color foregroundColor, Color backgroundColor) {
        JLabel newLabel = new JLabel(startingText);
        newLabel.setForeground(foregroundColor);
        newLabel.setBackground(backgroundColor);

        return newLabel;
    }

    static public JLabel createLabel(String startingText, Color foregroundColor) {
        return ComponentFactory.createLabel(startingText, foregroundColor, UIManager.getColor("Panel.background"));
    }

    static public JTextField createTextBox(int viewsize, Color foregroundColor, Color backgroundColor,
            DocumentFilter filter, Border border) {
        JTextField newTextBox = new JTextField(viewsize);
        newTextBox.setForeground(foregroundColor);
        newTextBox.setBackground(backgroundColor);

        if (filter != null) {
            ((AbstractDocument) newTextBox.getDocument()).setDocumentFilter(filter);
        }

        if (border != null) {
            newTextBox.setBorder(border);
        }

        return newTextBox;
    }

    static public JTextField createTextBox(int viewsize, Color foregroundColor, Color backgroundColor,
            DocumentFilter filter) {
        return ComponentFactory.createTextBox(viewsize, foregroundColor, backgroundColor, filter, null);
    }

    static public JTextField createTextBox(int viewsize, Color foregroundColor, Color backgroundColor) {
        return ComponentFactory.createTextBox(viewsize, foregroundColor, backgroundColor, null, null);
    }

    static public JPasswordField createPasswordBox(int viewsize, Color foregroundColor, Color backgroundColor,
            DocumentFilter filter, Border border) {
        JPasswordField newTextBox = new JPasswordField(viewsize);
        newTextBox.setForeground(foregroundColor);
        newTextBox.setBackground(backgroundColor);

        if (filter != null) {
            ((AbstractDocument) newTextBox.getDocument()).setDocumentFilter(filter);
        }

        if (border != null) {
            newTextBox.setBorder(border);
        }

        return newTextBox;
    }

    static public JCheckBox createCheckbox(String text, Color foregroundColor, Color backgroundColor) {
        JCheckBox checkBox = new JCheckBox(text);
        checkBox.setForeground(foregroundColor);
        checkBox.setBackground(backgroundColor);

        return checkBox;
    }
}
