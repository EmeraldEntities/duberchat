package duberchat.frames;

import java.awt.event.*;
import javax.swing.*;
import java.awt.*;
import duberchat.events.SerializableEvent;

/**
 * A {@code DynamicFrame} is used for frames that are dynamic - that is, they
 * can be destroyed and reloaded.
 * <p>
 * This class provides a generic {@link #destroy() destroy} method that works
 * with most frames, provided they do not need extra cleanup.
 * <p>
 * Created <b>2020-12-04</b>
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Joseph Wang
 * @see javax.swing.JFrame
 * @see duberchat.frames.Destroyable
 * @see duberchat.frames.Reloadable
 */
@SuppressWarnings("serial")
public abstract class DynamicFrame extends JFrame implements Reloadable, Destroyable {
    /**
     * Constructs a new {@code DynamicFrame}.
     * 
     * @param title the title of this frame.
     */
    public DynamicFrame(String title) {
        super(title);
    }

    public void destroy() {
        this.setVisible(false);
        this.dispose();
    }

    public abstract void reload();

    /**
     * {@inheritDoc}
     * <p>
     * By default, calling a reload with a source simply calls the other
     * {@link #reload() reload function}, and the source is discarded. However, the
     * frame may decide to implement different functionality with reloads with a
     * source.
     * 
     * @param source {@inheritDoc}
     */
    public void reload(SerializableEvent source) {
        this.reload();
    }
}
