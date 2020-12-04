package chatutil;

import java.awt.image.BufferedImage;
import java.io.Serializable;

public class User implements Serializable {
  static final long serialVersionUID = 1L;

  public static final int OFFLINE = 0;
  public static final int ONLINE = 1;
  public static final int AWAY = 2;
  public static final int DND = 3;
  public static final int TOTAL_STATUSES = 4;

  private int userId;
  private String username;
  private int status;
  private BufferedImage pfp;

  public User(String username, int userId) {
    this.userId = userId;
    this.username = username;

    this.status = ONLINE;
    this.pfp = null; // todo: make default pfp
  }

  public Object getUserId() {
    return this.userId;
  }

  public String getUsername() {
    return this.username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public int getStatus() {
    return this.status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public BufferedImage getPfp() {
    return this.pfp;
  }

  public void setPfp(BufferedImage pfp) {
    this.pfp = pfp;
  }
}
