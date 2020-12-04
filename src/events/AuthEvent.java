package events;

import java.util.EventObject;

public abstract class AuthEvent extends EventObject {
  static final long serialVersionUID = 1L;

  public AuthEvent(Object source) {
    super(source);
  }
}
