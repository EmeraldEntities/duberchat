package duberchat.gui.frames;

import java.awt.event.*;
import javax.swing.*;

import java.awt.*;

@SuppressWarnings("serial")
public class ServerFrame extends JFrame {
  private JTextArea textArea;
  private JScrollPane scrollPane;
  private JPanel mainPanel;

  public ServerFrame() {
    this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    this.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
            setExtendedState(JFrame.ICONIFIED);
        }
    });
    
    this.mainPanel = new JPanel();
    this.textArea = new JTextArea();
    this.scrollPane = new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                                      JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    this.textArea.setEditable(false); 
    this.mainPanel.add(scrollPane);
    this.add(mainPanel);
    this.setVisible(true);
    this.setSize(new Dimension(600, 600));
    this.mainPanel.setSize(this.getSize());
    this.scrollPane.setSize(this.getSize());
    this.mainPanel.setPreferredSize(this.getSize());
    this.scrollPane.setPreferredSize(this.getSize());
  }

  public JTextArea getTextArea() {
    return this.textArea;
  }
  
}
