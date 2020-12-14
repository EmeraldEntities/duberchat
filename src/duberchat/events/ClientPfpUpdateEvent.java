package duberchat.events;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.imageio.ImageIO;

/**
 * A {@code ClientPfpUpdateEvent} is an event that is created when a client
 * updates their profile picture.
 * <p>
 * Since <b>2020-12-13</b>.
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Joseph Wang, Paula Yuan
 */
public class ClientPfpUpdateEvent extends ClientProfileUpdateEvent {
    static final long serialVersionUID = 1L;
    protected transient BufferedImage newPfp;
    protected String pfpFormat;

    /**
     * Constructs a new {@code ClientProfileUpdateEvent}.
     * 
     * @param source The source of this event.
     */
    public ClientPfpUpdateEvent(Object source, BufferedImage img, String pfpFormat) {
        super(source);

        this.newPfp = img;
        this.pfpFormat = pfpFormat;
    }

    /**
     * Custom writeObject method because Images are not serializable.
     * 
     * @param out The output stream writing out this event.
     * @throws IOException
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        ImageIO.write(this.newPfp, this.pfpFormat, out);
    }
 
    /**
     * Custom readObject method because Images are not serializable.
     * 
     * @param in The input stream reading in this event.
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.newPfp = ImageIO.read(in);
    }

    public BufferedImage getNewPfp() {
        return this.newPfp;
    }

    public void setNewPfp(BufferedImage newPfp) {
        this.newPfp = newPfp;
    }

    public String getPfpFormat() {
        return this.pfpFormat;
    }

    public void setPfpFormat(String pfpFormat) {
        this.pfpFormat = pfpFormat;
    }
}
