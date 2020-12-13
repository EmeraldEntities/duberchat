package duberchat.gui.util;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

import java.util.EventObject;
import java.util.concurrent.ConcurrentLinkedQueue;

import duberchat.events.*;
import duberchat.gui.frames.MainFrame;
import duberchat.gui.frames.DynamicGridbagFrame;

public class FrameFactory {
    private FrameFactory() {
    }

    public static DynamicGridbagFrame createRequestFrame(String title, Color bgColor, JLabel msg, JTextField input,
            JButton submit) {

        return new RequestFrame(title, bgColor, msg, input, submit);
    }

    public static DynamicGridbagFrame createConfirmFrame(String title, Color bgColor, JLabel text,
            ActionListener action) {
        return new ConfirmFrame(title, bgColor, text, action);
    }

    @SuppressWarnings("serial")
    private static class RequestFrame extends DynamicGridbagFrame {
        public static final Dimension DEFAULT_SIZE = new Dimension(400, 200);

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

            addConstrainedComponent(text, mainPanel, layout, constraints, 0, 0, 1, 1, GridBagConstraints.HORIZONTAL,
                    GridBagConstraints.CENTER, new Insets(0, 0, 8, 0));
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

    @SuppressWarnings("serial")
    private static class ConfirmFrame extends DynamicGridbagFrame {
        public static final Dimension DEFAULT_SIZE = new Dimension(400, 200);

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

            JButton confirmButton = ComponentFactory.createButton("Yes", MainFrame.MAIN_COLOR, MainFrame.TEXT_COLOR);
            confirmButton.addActionListener(action);
            confirmButton.addActionListener(destroyFrameAction);

            JButton denyButton = ComponentFactory.createButton("No", MainFrame.MAIN_COLOR, MainFrame.TEXT_COLOR);
            denyButton.addActionListener(destroyFrameAction);

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
