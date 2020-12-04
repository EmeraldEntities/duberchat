package events;

public class ClientLoginEvent extends ClientEvent {
  static final long serialVersionUID = 1L;

  protected boolean isNewUser;
  protected String username;
  protected int hashedPassword;

  public ClientLoginEvent(Object source, boolean isNewUser, String username, String password) {
    super(source);

    this.isNewUser = isNewUser;
    this.username = username;
    this.hashedPassword = password.hashCode();
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
}
