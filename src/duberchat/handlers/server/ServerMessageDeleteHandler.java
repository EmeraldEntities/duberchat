package duberchat.handlers.server;

import duberchat.events.FileWriteEvent;
import duberchat.events.MessageDeleteEvent;
import duberchat.events.SerializableEvent;
import duberchat.handlers.Handleable;
import duberchat.server.ChatServer;
import duberchat.chatutil.*;

import java.io.*;
import java.util.Iterator;

public class ServerMessageDeleteHandler implements Handleable {
  private ChatServer server;

  public ServerMessageDeleteHandler(ChatServer server) {
    this.server = server;
  }

  public void handleEvent(SerializableEvent newEvent) {
    MessageDeleteEvent event = (MessageDeleteEvent) newEvent;
    Message toDelete = event.getMessage();
    Channel toDeleteFrom = server.getChannels().get(toDelete.getChannel().getChannelId());
    int index = toDeleteFrom.getMessages().indexOf(toDelete);
    Message serverToDelete = toDeleteFrom.getMessages().remove(index);
    User source = (User) event.getSource();

    String filePath = "data/channels/" + toDeleteFrom.getChannelId();
    try {
      // delete the message from its channel file
      server.getFileWriteQueue().add(new FileWriteEvent(toDeleteFrom, filePath));

      // give a message deletion event to all online users in the channel
      Iterator<User> itr = toDeleteFrom.getUsers().values().iterator();
      while (itr.hasNext()) {
        User user = itr.next();
        if (!server.getCurUsers().containsKey(user)) continue;
        ObjectOutputStream output = server.getCurUsers().get(user).getOutputStream();
        output.writeObject(new MessageDeleteEvent(new User(source), new Message(serverToDelete)));
        output.flush();
      }
      server.getServerFrame().getTextArea().append("A message was removed from channel " + toDeleteFrom.getChannelId()
          + " by " + source.getUsername() + " and events sent to users\n");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
}
