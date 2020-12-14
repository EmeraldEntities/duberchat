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
    Channel serverChannel = server.getChannels().get(edited.getChannel().getChannelId());
    User source = server.getAllUsers().get(((User) event.getSource()).getUsername());
    for (Message msg : serverChannel.getMessages()) {
      if (msg.equals(edited)) {
        msg.setMessage(edited.getMessage());
        break;
      }
    }

    int id = serverChannel.getChannelId();
    server.getFileWriteQueue().add(new FileWriteEvent(serverChannel, "data/channels/" + id));
    Iterator<User> itr = serverChannel.getUsers().values().iterator();
    while (itr.hasNext()) {
      User user = itr.next();
      if (!server.getCurUsers().containsKey(user)) {
        continue;
      }
      ObjectOutputStream output = server.getCurUsers().get(user).getOutputStream();
      try {
        output.writeObject(new MessageEditEvent(new User(source), edited));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    server.getServerFrame().getTextArea()
        .append(source.getUsername() + " edited a message in channel " + id + ".\n");
  }
  
}
