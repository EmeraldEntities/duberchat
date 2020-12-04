package events;

public class ClientStatusUpdateEvent extends ClientEvent {
  static final long serialVersionUID = 1L;

  public static final int OFFLINE = 0;
  public static final int ONLINE = 1;
  public static final int AWAY = 2;
  public static final int DND = 3;
  public static final int TOTAL_STATUSES = 4;

  protected int status;

  public ClientStatusUpdateEvent(Object source, int status) {
    super(source);

    this.status = status;
  }

  public int getStatus() {
    return this.status;
  }
}
