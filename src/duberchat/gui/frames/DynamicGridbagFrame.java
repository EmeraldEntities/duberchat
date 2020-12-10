package duberchat.gui.frames;

import java.awt.*;

/**
 * A {@code DynamicGridbagFrame} is a {@code DynamicFrame} that supports a
 * {@code GridBagLayout} for one or multiple of its components.
 * <p>
 * Common functions to assist with JPanels that use {@code GridBagLayout} exist
 * in this class. This class serves only as a general base class for other, more
 * specific frames to extend and inherit common {@code GridBagLayout} methds.
 * <p>
 * Created <b>2020-12-04</b>
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Joseph Wang
 * @see duberchat.frames.DynamicFrame
 * @see javax.swing.JFrame
 * @see java.awt.GridBagLayout
 */
@SuppressWarnings("serial")
public abstract class DynamicGridbagFrame extends DynamicFrame {
    /**
     * Constructs a new {@code DynamicGridbagFrame}.
     * 
     * @param title the title of this frame.
     */
    public DynamicGridbagFrame(String title) {
        super(title);
    }

    public static void addConstrainedComponent(Component comp, Container cont, GridBagLayout layout,
            GridBagConstraints gbc, int rows, int cols, int width, int height, double weightX, double weightY, int fill,
            int anchor, Insets insets) {

        gbc.gridx = rows;
        gbc.gridy = cols;

        gbc.gridheight = height;
        gbc.gridwidth = width;

        gbc.fill = fill;
        gbc.insets = insets;
        gbc.anchor = anchor;

        gbc.weightx = weightX;
        gbc.weighty = weightY;

        layout.setConstraints(comp, gbc);
        cont.add(comp);
    }

    public static void addConstrainedComponent(Component comp, Container cont, GridBagLayout layout,
            GridBagConstraints gbc, int rows, int cols, int width, int height, int fill, int anchor, Insets insets) {

        addConstrainedComponent(comp, cont, layout, gbc, rows, cols, width, height, 0.0, 0.0, GridBagConstraints.NONE,
                GridBagConstraints.CENTER, insets);
    }

    public static void addConstrainedComponent(Component comp, Container cont, GridBagLayout layout,
            GridBagConstraints gbc, int rows, int cols, int width, int height) {
        addConstrainedComponent(comp, cont, layout, gbc, rows, cols, width, height, GridBagConstraints.NONE,
                GridBagConstraints.CENTER, new Insets(0, 0, 0, 0));
    }
}
