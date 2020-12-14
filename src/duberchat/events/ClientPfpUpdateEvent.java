package duberchat.events;

import java.awt.image.BufferedImage;

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
    protected BufferedImage newPfp;
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
