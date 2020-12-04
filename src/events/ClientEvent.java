package events;

import java.util.EventObject;

public abstract class ClientEvent extends EventObject {
  static final long serialVersionUID = 1L;

  public ClientEvent(Object source) {
    super(source);
  }
}
