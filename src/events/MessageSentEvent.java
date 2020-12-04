package events;

import chatutil.Message;

public class MessageSentEvent extends MessageEvent {
  static final long serialVersionUID = 1L;

  public MessageSentEvent(Object source, Message message) {
    super(source, message);
  }
}
