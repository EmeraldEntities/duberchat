package events;

public class AuthFailedEvent extends AuthEvent {
  static final long serialVersionUID = 1L;

  public AuthFailedEvent(Object source) {
    super(source);
  }
}