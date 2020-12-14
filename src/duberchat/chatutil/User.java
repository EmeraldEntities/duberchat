package duberchat.chatutil;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.Serializable;
import java.util.HashSet;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

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
    static final long serialVersionUID = 15L;

    /** An int constant representing an offline user. */
    public static final int OFFLINE = 0;
    /** An int constant representing an online user. */
    public static final int ONLINE = 1;
    /** An int constant representing an away user. */
    public static final int AWAY = 2;
    /** An int constant representing a DND user. */
    public static final int DND = 3;
    /** The total amount of statuses that can be set. */
    public static final int TOTAL_STATUSES = 4;

    /** The user's username. */
    private String username;
    /** The user's password, hashed for security. */
    private long hashedPassword;
    /** The user's status, as an int constant. */
    private int status;
    /** The user's profile picture. */
    private transient BufferedImage pfp;
    /** The format that the user's profile picture is in. */
    private String pfpFormat;
    /** The channels this user is in. */
    private HashSet<Integer> channels;
    /** The friends that this user has. */
    private HashSet<String> friends;

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
            this.pfpFormat = "png";
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.channels = new HashSet<>();
        this.friends = new HashSet<>();
    }

    /**
     * Constructor for a user given another user. In effect, makes a deep copy of
     * the given user.
     * 
     * @param user The user to copy from.
     */
    public User(User user) {
        this.username = user.getUsername();
        this.hashedPassword = user.getHashedPassword();
        this.channels = new HashSet<>(user.getChannels());
        this.pfp = copyImage(user.getPfp());
        this.pfpFormat = user.getPfpFormat();
        this.status = user.getStatus();
        this.friends = new HashSet<>(user.getFriends());
    }

    /**
     * Deep copies a {@code BufferedImage} and returns the copied image.
     * 
     * @param image the {@code BufferedImage} to copy.
     * @return a copied version of the provided image.
     * @see java.awt.image.BufferedImage
     */
    private static BufferedImage copyImage(BufferedImage image) {
        ColorModel cm = image.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = image.copyData(image.getRaster().createCompatibleWritableRaster());
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
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
     * @param out The output stream writing out this event.
     * @throws IOException
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();

        ByteArrayOutputStream bufferStream = new ByteArrayOutputStream();
        ImageIO.write(this.pfp, this.pfpFormat, bufferStream);

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
        this.pfp = ImageIO.read(new ByteArrayInputStream(bytes));
        // this.pfp = ImageIO.read(in);
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
     * Sets this user's hashed password to a specified one.
     * 
     * @param hashedPassword this user's new hashed password.
     */
    public void setHashedPassword(long hashedPassword) {
        this.hashedPassword = hashedPassword;
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
        return User.findStringStatus(this.status);
    }

    /**
     * Converts an int constant into a string status.
     * 
     * @param status an int with the status to turn into a string.
     * @return a string representing the provided status.
     */
    public static String findStringStatus(int status) {
        switch (status) {
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
     * Converts a string status back into an int constant, or {@code -1} if not found.
     * 
     * @param status the string status to be look for.
     * @return the associated int constant, or {@code -1} if not found.
     */
    public static int findIntStatus(String status) {
        if (status.equals("offline")) {
            return User.OFFLINE;
        }
        if (status.equals("online")) {
            return User.ONLINE;
        }
        if (status.equals("away")) {
            return User.AWAY;
        }
        if (status.equals("DND")) {
            return User.DND;
        }

        return -1;
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
     * Retrieves the file format of this user's profile picture.
     * 
     * @return String, the profile picture's format.
     */
    public String getPfpFormat() {
        return this.pfpFormat;
    }

    /**
     * Sets this pfp format to a new format.
     * 
     * @param newFormat the new pfp format.
     */
    public void setPfpFormat(String newFormat) {
        this.pfpFormat = newFormat;
    }

    /**
     * Retrieves the set of channels this user is in.
     * 
     * @return HashSet<Integer>, the set of channel ids representing the channels the user is in.
     */
    public HashSet<Integer> getChannels() {
        return this.channels;
    }

    /**
     * Retrieves the set of friends this user has.
     * 
     * @return HashSet<String>, the set of usernames of friends this user has.
     */
    public HashSet<String> getFriends() {
        return this.friends;
    }
}
