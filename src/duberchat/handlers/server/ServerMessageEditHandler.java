package duberchat.handlers.server;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Iterator;

import duberchat.chatutil.*;
import duberchat.events.FileWriteEvent;
import duberchat.events.MessageEditEvent;
import duberchat.events.SerializableEvent;
import duberchat.handlers.Handleable;
import duberchat.server.ChatServer;

public class ServerMessageEditHandler implements Handleable {
  private ChatServer server;

  public ServerMessageEditHandler(ChatServer server) {
    this.server = server;
  }

  public void handleEvent(SerializableEvent newEvent) {
    MessageEditEvent event = (MessageEditEvent) newEvent;
    Message edited = event.getMessage();
    Channel channel = server.getChannels().get(edited.getChannelId());
    String source = (String) event.getSource();
    for (Message msg : channel.getMessages()) {
      if (msg.equals(edited)) {
        msg.setMessage(edited.getMessage());
        break;
      }
    }

    int id = channel.getChannelId();
    server.getFileWriteQueue().add(new FileWriteEvent(channel, "data/channels/" + id));
    Iterator<User> itr = channel.getUsers().values().iterator();
    while (itr.hasNext()) {
      User user = itr.next();
      if (!server.getCurUsers().containsKey(user)) {
        continue;
      }
      ObjectOutputStream output = server.getCurUsers().get(user).getOutputStream();
      try {
        output.writeObject(new MessageEditEvent(source, edited));
        output.flush();
        output.reset();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    server.getServerFrame().getTextArea().append(source + " edited a message in channel " + id + ".\n");
  }
  
}
