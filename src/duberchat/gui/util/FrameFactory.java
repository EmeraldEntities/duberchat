package duberchat.gui.util;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Color;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;

import duberchat.gui.frames.MainFrame;
import duberchat.gui.frames.DynamicGridbagFrame;

/**
 * This {@code FrameFactory} class is a static factory class designed to provide
 * easy, simple creation of common styled frames.
 * <p>
 * This factory provides easy creation of frames that share common layouts and
 * are likely to be often used, like request or confirm frames. As the contents
 * of the frames tend to be radically different from one another, this class
 * will not hand out shared references if possible, and will prefer to create
 * and return new instances.
 * <p>
 * As the factory only operates on static methods, it is designed to be
 * non-initializable.
 * <p>
 * Created <b>2020-12-09</b>
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Joseph Wang
 * @see javax.swing.JFrame
 */
public class FrameFactory {
    /**
     * Prevents anyone from constructing a new {@FrameFactory}.
     */
    private FrameFactory() {
    }

    /**
     * Creates a {@link duberchat.gui.frames.DynamicGridbagFrame
     * DynamicGridbagFrame} in the style of a request frame format, complete with a
     * submit button, description, and text field.
     * <p>
     * Upon submitting, the frame will destroy itself, but any actions attached to
     * the listener beforehand will be performed. <b> Make sure to attach listeners
     * before creating this request frame</b>, or the event may be consumed and the
     * attached listener is not guaranteed to fire.
     * <p>
     * This method is designed to pair well with
     * {@link duberchat.gui.util.ComponentFactory ComponentFactory}, but any made
     * components will suffice as well.
     * 
     * @param title   the title of this frame.
     * @param bgColor the background color of this frame.
     * @param msg     the descriptive text for this frame.
     * @param input   the input field for this frame.
     * @param submit  the submit button for this frame, with an
     *                {@code ActionListener} for some functionality.
     * @return a {@code DynamicGridbagFrame} with the request layout and the
     *         specified parameters.
     * @see java.awt.event.ActionListener
     * @see duberchat.gui.frames.DynamicGridbagFrame
     */
    public static DynamicGridbagFrame createRequestFrame(String title, Color bgColor, JLabel msg, JTextField input,
            JButton submit) {

        return new RequestFrame(title, bgColor, msg, input, submit);
    }

    /**
     * Creates a {@link duberchat.gui.frames.DynamicGridbagFrame
     * DynamicGridbagFrame} in the style of a confirmation frame format, complete
     * with a yes/no buttons and a description.
     * <p>
     * Upon confirming, the frame will destroy itself. The specified
     * {@code ActionListener} and its {@code actionPerformed} method will be
     * invoked, but only if the user selects yes. Pressing no or closing the frame
     * will do nothing except destroy this frame.
     * <p>
     * This method is designed to pair well with
     * {@link duberchat.gui.util.ComponentFactory ComponentFactory}, but any made
     * components will suffice as well.
     * 
     * @param title   the title of this frame.
     * @param bgColor the background color of this frame.
     * @param text    the descriptive text for this frame.
     * @param action  the {@code ActionListener} to attach to the yes button.
     * @return a {@code DynamicGridbagFrame} with the confirm layout and the
     *         specified parameters.
     * @see java.awt.event.ActionListener
     * @see duberchat.gui.frames.DynamicGridbagFrame
     */
    public static DynamicGridbagFrame createConfirmFrame(String title, Color bgColor, JLabel text,
            ActionListener action) {
        return new ConfirmFrame(title, bgColor, text, action);
    }

    /**
     * A {@code RequestFrame} is a frame that is designed to assist in creating the
     * request layout needed for some methods.
     * <p>
     * Created <b>2020-12-09</b>
     * 
     * @since 1.0.0
     * @version 1.0.0
     * @author Joseph Wang
     * @see javax.swing.JFrame
     */
    @SuppressWarnings("serial")
    private static class RequestFrame extends DynamicGridbagFrame {
        /** The default size of this frame. */
        public static final Dimension DEFAULT_SIZE = new Dimension(400, 200);

        /**
         * Constructs a new {@code RequestFrame} with specified parameters.
         * 
         * @param title           the title of this frame.
         * @param backgroundColor the background color of this frame.
         * @param text            the description text for this frame.
         * @param input           the input field for this frame.
         * @param submitButton    the submit button for this frame.
         */
        public RequestFrame(String title, Color backgroundColor, JLabel text, JTextField input, JButton submitButton) {
            super(title);

            this.setResizable(false);
            this.setSize(RequestFrame.DEFAULT_SIZE);
            this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            this.setIconImage(new ImageIcon("data/system/logo.png").getImage());

            GridBagLayout layout = new GridBagLayout();
            GridBagConstraints constraints = new GridBagConstraints();

            JPanel mainPanel = new JPanel();
            mainPanel.setBackground(backgroundColor);
            mainPanel.setLayout(layout);
            mainPanel.setSize(this.getSize());

            // Ensure that even if the user forgets to destroy this component on press, that
            // this action listener will properly dispose of this component.
            submitButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    reload();
                }
            });
            this.getRootPane().setDefaultButton(submitButton);

            // Add components
            addConstrainedComponent(text, mainPanel, layout, constraints, 0, 0, 1, 1, GridBagConstraints.HORIZONTAL,
                    GridBagConstraints.CENTER, new Insets(16, 0, 8, 0));
            addConstrainedComponent(input, mainPanel, layout, constraints, 0, 1, 1, 1, GridBagConstraints.HORIZONTAL,
                    GridBagConstraints.CENTER, new Insets(0, 0, 16, 0));
            addConstrainedComponent(submitButton, mainPanel, layout, constraints, 0, 2, 1, 1,
                    GridBagConstraints.REMAINDER, GridBagConstraints.CENTER, new Insets(8, 0, 0, 0));

            this.add(mainPanel);
        };

        /**
         * {@inheritDoc}
         * 
         * Reloading a request frame will simply destroy the frame.
         */
        public void reload() {
            super.destroy();
        };
    }

    /**
     * A {@code ConfirmFrame} is a frame that is designed to assist in creating the
     * confirmation layout needed for some methods.
     * <p>
     * Created <b>2020-12-09</b>
     * 
     * @since 1.0.0
     * @version 1.0.0
     * @author Joseph Wang
     * @see javax.swing.JFrame
     */
    @SuppressWarnings("serial")
    private static class ConfirmFrame extends DynamicGridbagFrame {
        /** The default size of this frame. */
        public static final Dimension DEFAULT_SIZE = new Dimension(400, 200);

        /**
         * Constructs a new {@code ConfirmFrame} with the specified parameters.
         * 
         * @param title           the title of this frame.
         * @param backgroundColor the background colour of this frame.
         * @param text            the description text of this frame.
         * @param action          the {@code ActionListener} to attach to the yes
         *                        button.
         * @see java.awt.event.ActionListener
         */
        public ConfirmFrame(String title, Color backgroundColor, JLabel text, ActionListener action) {
            super(title);

            this.setResizable(false);
            this.setSize(ConfirmFrame.DEFAULT_SIZE);
            this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            this.setIconImage(new ImageIcon("data/system/logo.png").getImage());

            GridBagLayout layout = new GridBagLayout();
            GridBagConstraints constraints = new GridBagConstraints();

            JPanel mainPanel = new JPanel();
            mainPanel.setBackground(backgroundColor);
            mainPanel.setLayout(layout);
            mainPanel.setSize(this.getSize());

            ActionListener destroyFrameAction = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    // Make sure this frame is destroyed at the end.
                    reload();
                }
            };

            // Create the buttons
            JButton confirmButton = ComponentFactory.createButton("Yes", MainFrame.MAIN_COLOR, MainFrame.TEXT_COLOR);
            confirmButton.addActionListener(action);
            confirmButton.addActionListener(destroyFrameAction);

            JButton denyButton = ComponentFactory.createButton("No", MainFrame.MAIN_COLOR, MainFrame.TEXT_COLOR);
            denyButton.addActionListener(destroyFrameAction);

            // Add components
            addConstrainedComponent(text, mainPanel, layout, constraints, 0, 0, 2, 1, GridBagConstraints.HORIZONTAL,
                    GridBagConstraints.CENTER, new Insets(0, 0, 8, 0));
            addConstrainedComponent(confirmButton, mainPanel, layout, constraints, 0, 1, 1, 1, 1.0, 1.0,
                    GridBagConstraints.HORIZONTAL, GridBagConstraints.LINE_END, new Insets(8, 8, 8, 8));
            addConstrainedComponent(denyButton, mainPanel, layout, constraints, 1, 1, 1, 1, 1.0, 1.0,
                    GridBagConstraints.HORIZONTAL, GridBagConstraints.LINE_START, new Insets(8, 8, 8, 8));

            this.add(mainPanel);
        }

        /**
         * {@inheritDoc}
         * 
         * Reloading a confirm frame will simply destroy the frame.
         */
        public void reload() {
            super.destroy();
        };
    }
}
