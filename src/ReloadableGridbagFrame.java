import javax.swing.*;
import java.awt.*;

@SuppressWarnings("serial")
public abstract class ReloadableGridbagFrame extends JFrame implements Reloadable {
    public ReloadableGridbagFrame(String title) {
        super(title);
    }

    public void addConstrainedComponent(Component component, Container container, GridBagLayout layout,
            GridBagConstraints constraints, int gridX, int gridY, int gridWidth, int gridHeight, int fill) {
        constraints.gridx = gridX;
        constraints.gridy = gridY;

        constraints.gridheight = gridHeight;
        constraints.gridwidth = gridWidth;

        constraints.fill = fill;

        layout.setConstraints(component, constraints);
        container.add(component);
    }

    public void addConstrainedComponent(Component component, Container container, GridBagLayout layout,
            GridBagConstraints constraints, int gridX, int gridY, int gridWidth, int gridHeight) {
        this.addConstrainedComponent(component, container, layout, constraints, gridX, gridY, gridWidth, gridHeight,
                GridBagConstraints.NONE);
    }

    public abstract void reload();
}
