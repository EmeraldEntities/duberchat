package duberchat.handlers.server;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Iterator;

import duberchat.events.ChannelDeleteEvent;
import duberchat.events.FileWriteEvent;
import duberchat.events.SerializableEvent;
import duberchat.handlers.Handleable;
import duberchat.server.ChatServer;
import duberchat.chatutil.Channel;
import duberchat.chatutil.User;

public class ServerChannelDeleteHandler implements Handleable {
  private ChatServer server;

  public ServerChannelDeleteHandler(ChatServer server) {
    this.server = server;
  }

  public void handleEvent(SerializableEvent newEvent) {
    ChannelDeleteEvent event = (ChannelDeleteEvent) newEvent;
    int toDeleteId = event.getChannel().getChannelId();
    Channel serverToDelete = server.getChannels().get(toDeleteId);

    try {
      // Remove the channel from all its users and their files
      Iterator<User> itr = serverToDelete.getUsers().values().iterator();
      while (itr.hasNext()) {
        User user = itr.next();
        user.getChannels().remove(toDeleteId);
        String filePath = "data/users/" + user.getUsername();
        server.getFileWriteQueue().add(new FileWriteEvent(user, filePath));

        // Give back a channel deletion event to all currently online users in the channel
        if (!server.getCurUsers().containsKey(user)) continue;
        ObjectOutputStream output = server.getCurUsers().get(user).getOutputStream();
        output.writeObject(new ChannelDeleteEvent(new User((User) event.getSource()), 
                                                  new Channel(serverToDelete)));
        output.flush();
      }

      //Remove the channel file
      File channelFile = new File("data/channels/" + toDeleteId);
      channelFile.delete();
      server.getServerFrame().getTextArea().append("channel " + toDeleteId + " was deleted\n");
    } catch (IOException e) {
      e.printStackTrace();
    }
    
    // remove the channel from the server's map of channels
    server.getChannels().remove(toDeleteId);
  }
  
}
