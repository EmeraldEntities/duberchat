package duberchat.gui.frames;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Component;
import java.awt.Container;

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

    /**
     * Adds a constrained component to a specified container, with a specified
     * layout and constraints.
     * <p>
     * The component will be added according to the parameters set. This method
     * exists to prevent repeat of common code, at the expense of looking absolutely
     * terrifying.
     * <p>
     * See {@link java.awt.GridBagLayout GridBagLayout} and
     * {@link java.awt.GridBagConstraints GridBagConstraints} for a more detailed
     * explanation about each parameter.
     * 
     * @param comp    the component to add to the container.
     * @param cont    the container to be added to.
     * @param layout  the layout of the container.
     * @param gbc     the shared GridBagConstraints object.
     * @param rows    the grid x position to start at.
     * @param cols    the grid y position to start at.
     * @param width   the amount of columns the component should take up.
     * @param height  the amount of rows the component should take up.
     * @param weightX the weight of the component in the x direction.
     * @param weightY the weight of the component in the y direction.
     * @param fill    the direction to fill the component if possible.
     * @param anchor  the anchored start location of the component.
     * @param insets  any {@code Insets} to specify spacing.
     */
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

    /**
     * Adds a constrained component to a specified container, with a specified
     * layout and constraints.
     * <p>
     * The component will be added according to the parameters set. This method
     * exists to prevent repeat of common code, at the expense of looking absolutely
     * terrifying.
     * <p>
     * See {@link java.awt.GridBagLayout GridBagLayout} and
     * {@link java.awt.GridBagConstraints GridBagConstraints} for a more detailed
     * explanation about each parameter.
     * <p>
     * This method will assign default values (0.0) to the weight of both
     * directions. If more precise control over the component is required, consider
     * using
     * {@link #addConstrainedComponent(Component, Container, GridBagLayout, GridBagConstraints, int, int, int, int, double, double, int, int, Insets)}
     * instead.
     * 
     * @param comp   the component to add to the container.
     * @param cont   the container to be added to.
     * @param layout the layout of the container.
     * @param gbc    the shared GridBagConstraints object.
     * @param rows   the grid x position to start at.
     * @param cols   the grid y position to start at.
     * @param width  the amount of columns the component should take up.
     * @param height the amount of rows the component should take up.
     * @param fill   the direction to fill the component if possible.
     * @param anchor the anchored start location of the component.
     * @param insets any {@code Insets} to specify spacing.
     */
    public static void addConstrainedComponent(Component comp, Container cont, GridBagLayout layout,
            GridBagConstraints gbc, int rows, int cols, int width, int height, int fill, int anchor, Insets insets) {

        addConstrainedComponent(comp, cont, layout, gbc, rows, cols, width, height, 0.0, 0.0, GridBagConstraints.NONE,
                GridBagConstraints.CENTER, insets);
    }

    /**
     * Adds a constrained component to a specified container, with a specified
     * layout and constraints.
     * <p>
     * The component will be added according to the parameters set. This method
     * exists to prevent repeat of common code, at the expense of looking absolutely
     * terrifying.
     * <p>
     * See {@link java.awt.GridBagLayout GridBagLayout} and
     * {@link java.awt.GridBagConstraints GridBagConstraints} for a more detailed
     * explanation about each parameter.
     * <p>
     * This method will assign default values (0.0) to the weight of both
     * directions, and use default values for fill, anchor, and insets
     * ({@link java.awt.GridBagConstraints#NONE},
     * {@link java.awt.GridBagConstraints#CENTER}, and a new Insets object with 0
     * for all directions respectively).
     * <p>
     * If more precise control over the component is required, consider using
     * {@link #addConstrainedComponent(Component, Container, GridBagLayout, GridBagConstraints, int, int, int, int, double, double, int, int, Insets)}
     * instead.
     * 
     * @param comp   the component to add to the container.
     * @param cont   the container to be added to.
     * @param layout the layout of the container.
     * @param gbc    the shared GridBagConstraints object.
     * @param rows   the grid x position to start at.
     * @param cols   the grid y position to start at.
     * @param width  the amount of columns the component should take up.
     * @param height the amount of rows the component should take up.
     */
    public static void addConstrainedComponent(Component comp, Container cont, GridBagLayout layout,
            GridBagConstraints gbc, int rows, int cols, int width, int height) {
        addConstrainedComponent(comp, cont, layout, gbc, rows, cols, width, height, GridBagConstraints.NONE,
                GridBagConstraints.CENTER, new Insets(0, 0, 0, 0));
    }
}
