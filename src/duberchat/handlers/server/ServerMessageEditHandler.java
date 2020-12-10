package duberchat.handlers.server;

import java.io.IOException;
import java.io.ObjectOutputStream;

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
    Channel serverChannel = server.getChannels().get(edited.getChannel().getChannelId());
    for (Message msg : serverChannel.getMessages()) {
      if (msg.equals(edited)) {
        msg.setMessage(edited.getMessage());
        break;
      }
    }
    int id = serverChannel.getChannelId();
    server.getFileWriteQueue().add(new FileWriteEvent(serverChannel, "data/channels/" + id + ".txt"));
    for (User user : serverChannel.getUsers()) {
      if (!server.getCurUsers().containsKey(user)) {
        continue;
      }
      ObjectOutputStream output = server.getCurUsers().get(user).getOutputStream();
      try {
        output.writeObject(new MessageEditEvent((User) event.getSource(), edited));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
  
}
