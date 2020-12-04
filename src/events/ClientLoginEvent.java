package events;

public class ClientLoginEvent extends ClientEvent {
  static final long serialVersionUID = 1L;

  protected boolean isNewUser;
  protected String username;
  protected int hashedPassword;
  protected int recentChannel;

  public ClientLoginEvent(Object source, boolean isNewUser, String username, String password, int recentChannel) {
    super(source);

    this.isNewUser = isNewUser;
    this.username = username;
    this.hashedPassword = password.hashCode();
    this.recentChannel = recentChannel;
  }

  public boolean getIsNewUser() {
    return this.isNewUser;
  }

  public String getUsername() {
    return this.username;
  }

  public int getHashedPassword() {
    return this.hashedPassword;
  }

  public int getRecentChannel() {
    return this.recentChannel;
  }
}
