package duberchat.frames;

/**
 * The {@code Reloadable} interface is for frames that are able to reload their
 * content.
 * <p>
 * A reload may be implemented in a variety of ways but the method is design to
 * serve as a bridge between the frame and an external controlling class. The
 * method signifies to the frame that a state has changed or a trigger was hit,
 * and that it should be reloaded.
 * <p>
 * Created <b>2020-12-04</b>
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Joseph Wang
 * @see javax.swing.JFrame
 */
public interface Reloadable {
    /**
     * Requests a reload for a frame.
     * <p>
     * This can be as a result of a state change, an event trigger, etc. The
     * specific frame implementing should handle logic and can specify when to call
     * this function according to its documentation.
     */
    public void reload();
}