package duberchat.gui.util;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.AbstractDocument;
import javax.swing.text.DocumentFilter;

import java.awt.*;

/**
 * The {@code ComponentFactory} class is a static factory class designed to
 * provide easy, simple creation of common JComponents for panels.
 * <p>
 * This factory provides common constructor methods for JLabels, JButtons,
 * JTextFields, etc. and are designed to be as customizable as required. As
 * components tend to be radically different from one another, this class will
 * not hand out shared references if possible, and will prefer to create and
 * return new instances.
 * <p>
 * As the factory only operates on static methods, it is designed to be
 * non-initializable.
 * <p>
 * Created <b>2020-12-08</b>
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Joseph Wang
 * @see javax.swing.JFrame
 * @see javax.swing.JComponent
 */
public class ComponentFactory {
    public static Font buttonFont = new Font("Courier", Font.PLAIN, 12);

    /**
     * Prevents anyone from constructing a new {@ComponentFactory}.
     */
    private ComponentFactory() {
    }

    /**
     * Constructs a new {@link javax.swing.JButton JButton} based on the provided
     * parameters.
     * <p>
     * The {@code ActionListener} can be specified as {@code null} with no issue,
     * but it is preferred to use {@link #createButton(String, Color, Color)} if an
     * ActionListener cannot be provided.
     * 
     * @param startingText    the starting text on the button.
     * @param foregroundColor the foreground color (eg. text color) .
     * @param backgroundColor the background color.
     * @param onClick         the listener to attach to the button, to be called on
     *                        click.
     * @return a new {@code JButton} with properties from the provided params.
     * @see javax.swing.JButton
     * @see java.awt.event.ActionListener
     * @see java.awt.Color
     */
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

    /**
     * Constructs a new {@link javax.swing.JButton JButton} based on the provided
     * parameters.
     * <p>
     * To construct a {@code JButton} with a provided {@code ActionListener}, use
     * {@link #createButton(String, Color, Color, ActionListener)}.
     * 
     * @param startingText    the starting text on the button.
     * @param foregroundColor the foreground color (text color).
     * @param backgroundColor the background color.
     * 
     * @return a new {@code JButton} with properties from the provided params.
     * @see javax.swing.JButton
     * @see java.awt.Color
     */
    static public JButton createButton(String startingText, Color foregroundColor, Color backgroundColor) {
        return ComponentFactory.createButton(startingText, foregroundColor, backgroundColor, null);
    }

    /**
     * Constructs a new {@link javax.swing.JButton JButton} based on the provided
     * parameters.
     * <p>
     * To construct a {@code JButton} with provided colors, use
     * {@link #createButton(String, Color, Color, ActionListener)}. This method
     * internally calls said method with the {@code UIManager} default colours for
     * {@code Button.foreground} and {@code Button.background}.
     * 
     * @param startingText the starting text on the button.
     * @param onClick      the listener to attach to the button, to be called on
     *                     click.
     * @return a new {@code JButton} with properties from the provided params.
     * @see javax.swing.JButton
     * @see javax.swing.UIManager
     * @see java.awt.event.ActionListener
     */
    static public JButton createButton(String startingText, ActionListener onClick) {
        return ComponentFactory.createButton(startingText, UIManager.getColor("Button.foreground"),
                UIManager.getColor("Button.background"), onClick);
    }

    /**
     * Constructs a new {@link javax.swing.JButton JButton} based on the provided
     * parameters.
     * <p>
     * To construct a {@code JButton} with provided colors and an
     * {@code ActionListener}, use
     * {@link #createButton(String, Color, Color, ActionListener)}. This method
     * internally calls said method with the {@code UIManager} default colours for
     * {@code Button.foreground} and {@code Button.background}.
     * 
     * @param startingText the starting text on the button.
     * 
     * @return a new {@code JButton} with properties from the provided params.
     * @see javax.swing.JButton
     * @see javax.swing.UIManager
     */
    static public JButton createButton(String startingText) {
        return ComponentFactory.createButton(startingText, UIManager.getColor("Button.foreground"),
                UIManager.getColor("Button.background"), null);
    }

    /**
     * Constructs a new {@link javax.swing.JLabel JLabel} based on the provided
     * parameters.
     * <p>
     * If the background color should be the default colour, consider using
     * {@link #createLabel(String, Color)} instead.
     * 
     * @param startingText    the starting text of the label.
     * @param foregroundColor the foreground color (text colour).
     * @param backgroundColor the background color.
     * 
     * @return a new {@code JLabel} with properties from the provided params.
     * @see javax.swing.JLabel
     * @see java.awt.Color
     */
    static public JLabel createLabel(String startingText, Color foregroundColor, Color backgroundColor) {
        JLabel newLabel = new JLabel(startingText);
        newLabel.setForeground(foregroundColor);
        newLabel.setBackground(backgroundColor);

        return newLabel;
    }

    /**
     * Constructs a new {@link javax.swing.JLabel JLabel} based on the provided
     * parameters.
     * <p>
     * To construct a {@code JLabel} with both colours, use
     * {@link #createLabel(String, Color, Color)}. This method internally uses the
     * {@UIManager} default colour for {@code Label.background}.
     * 
     * @param startingText    the starting text of the label.
     * @param foregroundColor the foreground color (text colour).
     * 
     * @return a new {@code JLabel} with properties from the provided params.
     * @see javax.swing.JLabel
     * @see java.awt.Color
     * @see javax.swing.UIManager
     */
    static public JLabel createLabel(String startingText, Color foregroundColor) {
        return ComponentFactory.createLabel(startingText, foregroundColor, UIManager.getColor("Label.background"));
    }

    /**
     * Constructs a new {@link javax.swing.JLabel JLabel} based on the provided
     * parameters.
     * <p>
     * To construct a {@code JLabel} with both colours, use
     * {@link #createLabel(String, Color, Color)}. This method internally uses the
     * {@UIManager} default colours for {@code Label.foreground} and
     * {@code Label.background}.
     * 
     * @param startingText the starting text of the label.
     * 
     * @return a new {@code JLabel} with properties from the provided params.
     * @see javax.swing.JLabel
     * @see javax.swing.UIManager
     */
    static public JLabel createLabel(String startingText) {
        return ComponentFactory.createLabel(startingText, UIManager.getColor("Label.foreground"),
                UIManager.getColor("Label.background"));
    }

    /**
     * Constructs a new {@link javax.swing.JTextField JTextField} based on the
     * provided parameters.
     * <p>
     * Both border and filter can be substituted for {@code null}, but it is
     * preferred to use either
     * {@link #createTextBox(int, Color, Color, DocumentFilter)} or
     * {@link #createTextBox(int, Color, Color)}, if possible.
     * 
     * @param viewsize        the view size of the textbox.
     * @param foregroundColor the foreground color (text color).
     * @param backgroundColor the background color.
     * @param filter          the filter this textbox uses.
     * @param border          a border for this textbox.
     * 
     * @return a new {@code JTextBox} with properties from the provided params.
     * @see javax.swing.JTextBox
     * @see java.awt.Color
     * @see javax.swing.text.DocumentFilter
     * @see javax.swing.border.Border
     */
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

    /**
     * Constructs a new {@link javax.swing.JTextField JTextField} based on the
     * provided parameters.
     * <p>
     * This method will provide no border to the returned JTextField. If a border is
     * desired, use
     * {@link #createTextBox(int, Color, Color, DocumentFilter, Border)}.
     * 
     * @param viewsize        the view size of the textbox.
     * @param foregroundColor the foreground color (text color).
     * @param backgroundColor the background color.
     * @param filter          the filter this textbox uses.
     * 
     * @return a new {@code JTextBox} with properties from the provided params.
     * @see javax.swing.JTextBox
     * @see java.awt.Color
     * @see javax.swing.text.DocumentFilter
     */
    static public JTextField createTextBox(int viewsize, Color foregroundColor, Color backgroundColor,
            DocumentFilter filter) {
        return ComponentFactory.createTextBox(viewsize, foregroundColor, backgroundColor, filter, null);
    }

    /**
     * Constructs a new {@link javax.swing.JTextField JTextField} based on the
     * provided parameters.
     * <p>
     * This method will provide no border or filter to the returned JTextField. If a
     * border and/or filter is desired, consider using
     * {@link #createTextBox(int, Color, Color, DocumentFilter, Border)}.
     * 
     * @param viewsize        the view size of the textbox.
     * @param foregroundColor the foreground color (text color).
     * @param backgroundColor the background color.
     * 
     * @return a new {@code JTextBox} with properties from the provided params.
     * @see javax.swing.JTextBox
     * @see java.awt.Color
     */
    static public JTextField createTextBox(int viewsize, Color foregroundColor, Color backgroundColor) {
        return ComponentFactory.createTextBox(viewsize, foregroundColor, backgroundColor, null, null);
    }

    /**
     * Constructs a new {@link javax.swing.JTextField JTextField} based on the
     * provided parameters.
     * <p>
     * This method will use the {@code UIManager} default colors for
     * {@code TextField.foreground} and {@code TextField.background}, and provides
     * no border or filter to the returned JTextField. If colours, a border, and
     * filter is desired, consider using
     * {@link #createTextBox(int, Color, Color, DocumentFilter, Border)}.
     * 
     * @param viewsize the view size of the textbox.
     * 
     * @return a new {@code JTextBox} with properties from the provided params.
     * @see javax.swing.JTextBox
     */
    static public JTextField createTextBox(int viewsize) {
        return ComponentFactory.createTextBox(viewsize, UIManager.getColor("TextField.foreground"),
                UIManager.getColor("TextField.background"), null, null);
    }

    /**
     * Constructs a new {@link javax.swing.JPasswordField JPasswordField} based on
     * the provided parameters.
     * <p>
     * If required, both filter and border can be set to null to specify that the
     * password field has no filter or border.
     * 
     * @param viewsize        the view size of the password field.
     * @param foregroundColor the foreground color (text color).
     * @param backgroundColor the background color.
     * @param filter          the filter this password field uses.
     * @param border          a border for this password field.
     * 
     * @return a new {@code JPasswordField} with properties from the provided
     *         params.
     * @see javax.swing.JPasswordField
     * @see java.awt.Color
     * @see javax.swing.text.DocumentFilter
     * @see javax.swing.border.Border
     */
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

    /**
     * Constructs a new {@link javax.swing.JCheckBox JCheckBox} based on the
     * provided parameters.
     * 
     * @param text            the associated text of the checkbox.
     * @param foregroundColor the foreground color (text color).
     * @param backgroundColor the background color.
     * 
     * @return a new {@code JCheckBox} with properties from the provided params.
     * @see javax.swing.JCheckBox
     * @see java.awt.Color
     */
    static public JCheckBox createCheckbox(String text, Color foregroundColor, Color backgroundColor) {
        JCheckBox checkBox = new JCheckBox(text);
        checkBox.setForeground(foregroundColor);
        checkBox.setBackground(backgroundColor);

        return checkBox;
    }

    // static public JOptionPane createRequestPane() {
    // JOptionPane pane = new JOptionPane("I want pain", JOptionPane.PLAIN_MESSAGE,
    // JOptionPane.OK_CANCEL_OPTION);

    // Component[] comps = pane.getComponents();
    // for (Component c : comps) {
    // System.out.println(c.getName() + " " + c.getClass());
    // }

    // return pane;
    // }
}
