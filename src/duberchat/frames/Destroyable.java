package duberchat.frames;

/**
 * The {@code Destroyable} interface is for JFrames that are able to be
 * destroyed.
 * <p>
 * Destroying a destroyable frame should result in a fail-free disposing of the
 * frame that can be called at any time, to indicate that the frame is no longer
 * needed.
 * <p>
 * Created <b>2020-12-05</b>
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Joseph Wang
 * @see javax.swing.JFrame
 */
public interface Destroyable {
    /**
     * Destroys this frame.
     * <p>
     * This method is intended to be the proper way to dispose of a frame, and thus
     * must be fail-safe.
     */
    public void destroy();
}
