package duberchat.gui.util;

import java.awt.event.ActionListener;

import javax.swing.JTextField;
import javax.swing.JCheckBox;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPasswordField;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.text.AbstractDocument;
import javax.swing.text.DocumentFilter;
import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.awt.Font;
import java.awt.Image;
import java.awt.Color;
import java.io.File;
import java.io.IOException;


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
 * As an added bonus, any changes made to methods of this class are immediately
 * shared across any component that uses one of these factory methods, allowing
 * for centralized and easy design configurations.
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
    /** The default font for buttons that come from this factory. */
    public static Font buttonFont = new Font("Courier", Font.PLAIN, 12);

    /**
     * Prevents anyone from constructing a new {@ComponentFactory}.
     */
    private ComponentFactory() {
    }

    /**
     * Attempts to load in and return an image icon with the requested width and
     * height from the specified image path.
     * 
     * @param imagePath the path of the image.
     * @param width     the width of the image, in px. This can be -1 to preserve
     *                  aspect ratio, but only if height is not -1.
     * @param height    the height of the image, in px. This can be -1 to preserve
     *                  aspect ratio, but only if width is not -1.
     * @return an {@ImageIcon} with the specified width and height from the image
     *         path.
     * @throws IOException if an error occured while reading the image.
     * @see javax.swing.ImageIcon
     */
    private static ImageIcon getIcon(String imagePath, int width, int height) throws IOException {
        File file = new File(imagePath);

        BufferedImage img = ImageIO.read(file);
        ImageIcon imgIcon = new ImageIcon(img.getScaledInstance(width, height, Image.SCALE_SMOOTH));

        return imgIcon;
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
    public static JButton createButton(String startingText, Color foregroundColor, Color backgroundColor,
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
     * {@link #createButton(String, Color, Color, ActionListener)}. Alternatively,
     * construct the button and attach the listener after.
     * 
     * @param startingText    the starting text on the button.
     * @param foregroundColor the foreground color (text color).
     * @param backgroundColor the background color.
     * 
     * @return a new {@code JButton} with properties from the provided params.
     * @see javax.swing.JButton
     * @see java.awt.Color
     */
    public static JButton createButton(String startingText, Color foregroundColor, Color backgroundColor) {
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
    public static JButton createButton(String startingText, ActionListener onClick) {
        return ComponentFactory.createButton(startingText, UIManager.getColor("Button.foreground"),
                UIManager.getColor("Button.background"), onClick);
    }

    /**
     * Constructs a new {@link javax.swing.JButton JButton} based on the provided
     * parameters.
     * <p>
     * To construct a {@code JButton} with provided colors and an
     * {@code ActionListener}, use
     * {@link #createButton(String, Color, Color, ActionListener)}. Alternatively,
     * if only the listener is desired, construct the button and attach the listener
     * after. This method internally calls said method with the {@code UIManager}
     * default colours for {@code Button.foreground} and {@code Button.background}.
     * 
     * @param startingText the starting text on the button.
     * 
     * @return a new {@code JButton} with properties from the provided params.
     * @see javax.swing.JButton
     * @see javax.swing.UIManager
     */
    public static JButton createButton(String startingText) {
        return ComponentFactory.createButton(startingText, UIManager.getColor("Button.foreground"),
                UIManager.getColor("Button.background"), null);
    }

    /**
     * Constructs a new {@link javax.swing.JButton JButton} with an icon based on
     * the provided parameters.
     * <p>
     * The {@code ActionListener} can be specified as {@code null} with no issue if
     * not needed, but it is preferred to use
     * {@link #createImageButton(String, String, int, int, Color, Color)} instead.
     * 
     * @param altText         the alt text on the button, if the image could not be
     *                        loaded.
     * @param imagePath       the path of the image.
     * @param width           the width of the new image, in px. This can be -1 to
     *                        preserve aspect ratio, but only if height is not -1.
     * @param height          the height of the new image, in px. This can be -1 to
     *                        preserve aspect ratio, but only if width is not -1.
     * @param foregroundColor the foreground color (eg. text color) .
     * @param backgroundColor the background color.
     * @param onClick         the listener to attach to the button, to be called on
     *                        click.
     * 
     * @return a new {@code JButton} with properties from the provided params and an
     *         icon.
     * @see javax.swing.JButton
     * @see java.awt.event.ActionListener
     * @see java.awt.Color
     */
    public static JButton createImageButton(String altText, String imagePath, int width, int height,
            Color foregroundColor, Color backgroundColor, ActionListener onClick) {
        JButton newButton = ComponentFactory.createButton(altText, foregroundColor, backgroundColor, onClick);

        try {
            newButton.setIcon(getIcon(imagePath, width, height));
            newButton.setText("");

        } catch (IOException e) {
            System.out.println("SYSTEM: Could not load button image!");
        }

        return newButton;
    }

    /**
     * Constructs a new {@link javax.swing.JButton JButton} with an icon based on
     * the provided parameters.
     * <p>
     * This method allows construction of an image button without needing to attach
     * an action listener. If an action listener is desired and attaching in this
     * method is also desired, consider using
     * {@link #createImageButton(String, String, int, int, Color, Color, ActionListener)}
     * instead. Alternatively, construct the button and attach the listener after.
     * 
     * @param altText         the alt text on the button, if the image could not be
     *                        loaded.
     * @param imagePath       the path of the image.
     * @param width           the width of the new image, in px. This can be -1 to
     *                        preserve aspect ratio, but only if height is not -1.
     * @param height          the height of the new image, in px. This can be -1 to
     *                        preserve aspect ratio, but only if width is not -1.
     * @param foregroundColor the foreground color (eg. text color) .
     * @param backgroundColor the background color.
     * 
     * @return a new {@code JButton} with properties from the provided params and an
     *         icon.
     * @see javax.swing.JButton
     * @see java.awt.Color
     */
    public static JButton createImageButton(String altText, String imagePath, int width, int height,
            Color foregroundColor, Color backgroundColor) {
        JButton newButton = ComponentFactory.createButton(altText, foregroundColor, backgroundColor, null);

        try {
            newButton.setIcon(getIcon(imagePath, width, height));
            newButton.setText("");

        } catch (IOException e) {
            System.out.println("SYSTEM: Could not load button image!");
        }

        return newButton;
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
    public static JLabel createLabel(String startingText, Color foregroundColor, Color backgroundColor) {
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
    public static JLabel createLabel(String startingText, Color foregroundColor) {
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
    public static JLabel createLabel(String startingText) {
        return ComponentFactory.createLabel(startingText, UIManager.getColor("Label.foreground"),
                UIManager.getColor("Label.background"));
    }

    /**
     * Constructs a new {@link javax.swing.JLabel JLabel} with an image based on the
     * provided parameters.
     * <p>
     * Background colour is automatically excluded as all images will be loaded
     * transparent.
     * 
     * @param altText         the starting text of the label.
     * @param imagePath       the path to the image.
     * @param width           the width of the image, in px. This can be -1 to
     *                        preserve aspect ratio, but only if height is not -1.
     * @param height          the height of the image, in px. This can be -1 to
     *                        preserve aspect ratio, but only if width is not -1.
     * @param foregroundColor the foreground color for text colour.
     * 
     * @return a new {@code JLabel} with properties from the provided params and an
     *         image.
     * @see javax.swing.JLabel
     * @see java.awt.Color
     * @see javax.swing.UIManager
     */
    public static JLabel createImageLabel(String altText, String imagePath, int width, int height,
            Color foregroundColor) {
        JLabel newLabel = ComponentFactory.createLabel(altText, foregroundColor,
                UIManager.getColor("Label.background"));

        try {
            newLabel.setIcon(getIcon(imagePath, width, height));
            newLabel.setText("");
        } catch (IOException e) {
            System.out.println("SYSTEM: Could not load label image!");
        }

        return newLabel;
    }

    /**
     * Constructs a new {@link javax.swing.JLabel JLabel} with an image based on the
     * provided parameters.
     * <p>
     * If a string path must be provided instead, use
     * {@link #createImageLabel(String, String, int, int, Color)}.
     * 
     * @param img the img to be displayed.
     * 
     * @return a new {@code JLabel} with the properly displayed image.
     * @see javax.swing.JLabel
     * @see java.awt.Color
     */
    public static JLabel createImageLabel(Image img) {
        JLabel newLabel = ComponentFactory.createLabel("", UIManager.getColor("Label.foreground"),
                UIManager.getColor("Label.background"));
        newLabel.setIcon(new ImageIcon(img));

        return newLabel;
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
    public static JTextField createTextBox(int viewsize, Color foregroundColor, Color backgroundColor,
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
    public static JTextField createTextBox(int viewsize, Color foregroundColor, Color backgroundColor,
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
    public static JTextField createTextBox(int viewsize, Color foregroundColor, Color backgroundColor) {
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
    public static JTextField createTextBox(int viewsize) {
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
    public static JPasswordField createPasswordBox(int viewsize, Color foregroundColor, Color backgroundColor,
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
    public static JCheckBox createCheckbox(String text, Color foregroundColor, Color backgroundColor) {
        JCheckBox checkBox = new JCheckBox(text);
        checkBox.setForeground(foregroundColor);
        checkBox.setBackground(backgroundColor);

        return checkBox;
    }

    /**
     * Constructs a new string {@link javax.swing.JComboBox JComboBox} based on the
     * provided parameters.
     * <p>
     * If needed, the {@code onChoice} parameter can be set to null, indicating no
     * action listener attached in this method.
     * 
     * @param strings         all the choices for this box.
     * @param selectedIndex   the starting index for this box.
     * @param foregroundColor the foreground color (text color).
     * @param backgroundColor the background color.
     * @param onChoice        an {@code ActionListener} to be added to this box.
     * 
     * @return a new {@code JComboBox} with properties from the provided params.
     * @see javax.swing.JComboBox
     * @see java.awt.Color
     * @see java.awt.event.ActionListener
     */
    public static JComboBox<String> createComboBox(String[] strings, int selectedIndex, Color foregroundColor,
            Color backgroundColor,
            ActionListener onChoice) {
        JComboBox<String> newComboBox = new JComboBox<>(strings);
        newComboBox.setSelectedIndex(selectedIndex);
        newComboBox.setBackground(backgroundColor);
        newComboBox.setForeground(foregroundColor);

        if (onChoice != null) {
            newComboBox.addActionListener(onChoice);
        }

        return newComboBox;
    }
}
