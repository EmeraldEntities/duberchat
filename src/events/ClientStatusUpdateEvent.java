package events;

public class ClientStatusUpdateEvent extends ClientEvent {
  static final long serialVersionUID = 1L;

  protected int status;

  public ClientStatusUpdateEvent(Object source, int status) {
    super(source);

    this.status = status;
  }

  public int getStatus() {
    return this.status;
  }
}
