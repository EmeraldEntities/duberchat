package duberchat.chatutil;

import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.imageio.ImageIO;

/**
 * This is the user class, representing one user in the server.
 * <p>
 * The user class contains all user-related and user-specific informtion.
 * <p>
 * 2020-12-03
 * 
 * @since 0.1
 * @version 0.1
 * @author Joseph Wang, Paula Yuan
 */
public class User implements Serializable {
    static final long serialVersionUID = 1L;

    public static final int OFFLINE = 0;
    public static final int ONLINE = 1;
    public static final int AWAY = 2;
    public static final int DND = 3;
    public static final int TOTAL_STATUSES = 4;

    private String username;
    private int status;
    private transient BufferedImage pfp;

    /**
     * Constructor for a user when no profile picture exists, used when first making
     * an account.
     * 
     * @param username The user's username.
     */
    public User(String username) {
        this.username = username;

        this.status = ONLINE;
        try {
            this.pfp = ImageIO.read(new File("data/images/default.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Constructor for a user when the profile picture exists.
     * 
     * @param username The user's username.
     * @param pfpPath  The file name of the user's profile picture.
     */
    public User(String username, String pfpPath) {
        this.username = username;

        this.status = ONLINE;
        try {
            this.pfp = ImageIO.read(new File("data/images/" + pfpPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Custom equals method because we need to know if the values are equal, not the references.
     * 
     * @param user The other user we're checking equality with.
     * @return boolean, whether the two users are the same (based on username)
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
     * @param status This user's new status
     */
    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * Retrieves this user's profile picture.
     * 
     * @return BufferedImag, the profile picture.
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
}
