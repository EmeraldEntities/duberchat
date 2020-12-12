package duberchat.chatutil;

import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.HashSet;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.imageio.ImageIO;

/**
 * A {@code User} represents a single user account, with all related information.
 * <p>
 * Each client is associated with one user. The user is also used as an identifier and 
 * passed around as client information.
 * 
 * <p>
 * Since <b>2020-12-04</b>.
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Joseph Wang, Paula Yuan
 */
public class User implements Serializable {
    static final long serialVersionUID = 4L;

    public static final int OFFLINE = 0;
    public static final int ONLINE = 1;
    public static final int AWAY = 2;
    public static final int DND = 3;
    public static final int TOTAL_STATUSES = 4;

    private String username;
    private long hashedPassword;
    private int status;
    private transient BufferedImage pfp;
    private HashSet<Integer> channels;

    /**
     * Constructor for a user when no profile picture exists, used when first making
     * an account.
     * 
     * @param username The user's username.
     * @param hashedPassword The user's password, hashed.
     */
    public User(String username, long hashedPassword) {
        this.username = username;
        this.hashedPassword = hashedPassword;

        this.status = ONLINE;
        try {
            this.pfp = ImageIO.read(new File("data/images/default.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.channels = new HashSet<>();
    }

    /**
     * Constructor for a user when additional information exists, used for existing users.
     * 
     * @param username The user's username.
     * @param hashedPassword The user's password, hashed.
     * @param pfpPath  The file name of the user's profile picture.
     * @param channels The set of all channel ids representing the channels the user is in.
     */
    public User(String username, long hashedPassword, String pfpPath, HashSet<Integer> channels) {
        this.username = username;
        this.hashedPassword = hashedPassword;
        this.channels = channels;

        this.status = ONLINE;
        try {
            this.pfp = ImageIO.read(new File("data/images/" + pfpPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Constructor for a user given another user. In effect, makes a copy of the given user.
     * 
     * @param user The user to copy from
     */
    public User(User user) {
        this.username = user.getUsername();
        this.hashedPassword = user.getHashedPassword();
        this.channels = user.getChannels();
        this.pfp = user.getPfp();
        this.status = user.getStatus();
    }

    /**
     * Checks if this user's profile picture is the same as some other image.
     * 
     * @param image The image we're checking against
     * @return boolean, whether the two images are the same 
     */
    public boolean pfpEquals(BufferedImage image) {
        // The images must be the same size.
        if (this.pfp.getWidth() != image.getWidth() || this.pfp.getHeight() != image.getHeight()) {
            return false;
        }

        int width = this.pfp.getWidth();
        int height = this.pfp.getHeight();

        // Loop over every pixel.
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Compare the pixels for equality.
                if (this.pfp.getRGB(x, y) != image.getRGB(x, y)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Custom equals method because we need to know if the values are equal, not the references.
     * 
     * @param obj The other object we're checking equality with.
     * @return boolean, whether the two objects are the same 
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } 
        if (obj == null || !(obj instanceof User)) {
            return false;
        }
        User user = (User) obj;
        return (this.username.equals(user.getUsername()));
    }

    @Override
    public int hashCode() {
        return this.username.hashCode();
    }

    /**
     * Custom writeObject method because Images are not serializable.
     * 
     * @param out The output stream writing out this user.
     * @throws IOException
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        ImageIO.write(this.pfp, "png", out);
    }
 
    /**
     * Custom readObject method because Images are not serializable.
     * 
     * @param in The input stream reading in this user.
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.pfp = ImageIO.read(in);
    }
 
    /**
     * Retrieves the user's username.
     * 
     * @return String, the username.
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * Assigns this user's username.
     * 
     * @param username This user's username.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Retrieves the user's hashed password.
     * 
     * @return long, the hashed password.
     */
    public long getHashedPassword() {
        return this.hashedPassword;
    }

    /**
     * Returns the user's status (e.g. online, offline, etc.)
                        
     * 
     * @return int, the user's status.
     */
    public int getStatus() {
        return this.status;
    }

    /**
     * Assigns the user's status.
     * 
     * @param status this user's new status
     */
    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * Retrieves this user's status, as a string.
     * 
     * @return a string with this user's status.
     */
    public String getStringStatus() {
        switch (this.status) {
            case OFFLINE:
                return "offline";
            case ONLINE:
                return "online";
            case AWAY:
                return "away";
            case DND:
                return "DND";
            default:
                return "unknown";
        }
    }

    /**
     * Retrieves this user's profile picture.
     * 
     * @return BufferedImage, the profile picture.
     */
    public BufferedImage getPfp() {
        return this.pfp;
    }

    /**
     * Assigns this user's profile picture.
     * 
     * @param pfp This user's profile picture.
     */
    public void setPfp(BufferedImage pfp) {
        this.pfp = pfp;
    }

    /**
     * Retrieves the set of channels this user is in.
     * 
     * @return HashSet<Integer>, the set of channel ids representing the channels the user is in.
     */
    public HashSet<Integer> getChannels() {
        return this.channels;
    }
}
