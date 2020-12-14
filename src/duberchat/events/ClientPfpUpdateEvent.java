package duberchat.events;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;


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

        ByteArrayOutputStream bufferStream = new ByteArrayOutputStream();
        ImageIO.write(this.newPfp, this.pfpFormat, bufferStream);

        byte[] bufferedBytes = bufferStream.toByteArray();
        out.writeObject(bufferedBytes);
        // ImageIO.write(this.pfp, this.pfpFormat, out);
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

        byte[] bytes = (byte[]) in.readObject();
        this.newPfp = ImageIO.read(new ByteArrayInputStream(bytes));
        // this.pfp = ImageIO.read(in);
    }

    /**
     * Retrieves this event's new pfp.
     * 
     * @return this event's new pfp.
     */
    public BufferedImage getNewPfp() {
        return this.newPfp;
    }

    /**
     * Sets this event's new pfp to another pfp.
     * 
     * @param newPfp this event's new pfp.
     */
    public void setNewPfp(BufferedImage newPfp) {
        this.newPfp = newPfp;
    }

    /**
     * Retrieves this event's pfp format.
     * 
     * @return this event's pfp format.
     */
    public String getPfpFormat() {
        return this.pfpFormat;
    }

    /**
     * Sets this event's pfp format.
     * 
     * @param pfpFormat this event's pfp format.
     */
    public void setPfpFormat(String pfpFormat) {
        this.pfpFormat = pfpFormat;
    }
}
