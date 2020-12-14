package duberchat.handlers.server;

import duberchat.events.ChannelRemoveMemberEvent;
import duberchat.events.FileWriteEvent;
import duberchat.events.RequestFailedEvent;
import duberchat.events.SerializableEvent;
import duberchat.handlers.Handleable;
import duberchat.server.ChatServer;

import java.io.*;
import java.util.Iterator;

import duberchat.chatutil.Channel;
import duberchat.chatutil.User;

public class ServerChannelRemoveMemberHandler implements Handleable {
  private ChatServer server;

  public ServerChannelRemoveMemberHandler(ChatServer server) {
    this.server = server;
  }

  public void handleEvent(SerializableEvent newEvent) {
    ChannelRemoveMemberEvent event = (ChannelRemoveMemberEvent) newEvent;
    int id = event.getChannel().getChannelId();
    Channel toDeleteFrom = server.getChannels().get(id);
    String username = event.getUsername();
    User toDelete = server.getAllUsers().get(username);
    User source = server.getAllUsers().get(((User) event.getSource()).getUsername());

    try {
      // If the user doesn't exist, send back a request failed event
      if (toDelete == null) {
        ObjectOutputStream output = server.getCurUsers().get(source).getOutputStream();
        output.writeObject(new RequestFailedEvent(new User(toDelete)));
        output.flush();
        server.getServerFrame().getTextArea()
            .append(source.getUsername() + " tried to remove a nonexistent user. Sent request failed event\n");
        return;
      }

      toDeleteFrom.removeUser(toDelete);
      toDelete.getChannels().remove(id);

      // Send back a message sent event to every online user in the channel, as well
      // as the removed user.
      ObjectOutputStream output = server.getCurUsers().get(toDelete).getOutputStream();

      // testing
      ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
      ObjectOutputStream objOut = new ObjectOutputStream(byteOut);
      objOut.writeObject(new ChannelRemoveMemberEvent(new User(toDelete), new Channel(toDeleteFrom), username));
      byte[] byteArr = byteOut.toByteArray();
      System.out.println("servr bytes: " + byteArr.length);
      ByteArrayInputStream byteIn = new ByteArrayInputStream(byteArr);
      ObjectInputStream objIn = new ObjectInputStream(byteIn);
      try {
        ChannelRemoveMemberEvent test = (ChannelRemoveMemberEvent) objIn.readObject();
        System.out.println("test" + test.getChannel().getUsers().size());
      } catch (ClassNotFoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      output.writeObject(new ChannelRemoveMemberEvent(new User(toDelete), new Channel(toDeleteFrom), username));
      output.flush();
      Iterator<User> itr = toDeleteFrom.getUsers().values().iterator();
      while (itr.hasNext()) {
        User member = itr.next();
        // skip offline users
        if (!server.getCurUsers().containsKey(member)) continue;
        output = server.getCurUsers().get(member).getOutputStream();
        Channel toSend = new Channel(toDeleteFrom);
        System.out.println(member.getUsername() + " " + toSend.getUsers().size());
        output.writeObject(new ChannelRemoveMemberEvent(new User(toDelete), 
                                                        new Channel(toDeleteFrom), 
                                                        username));
        output.flush();
      }
      server.getServerFrame().getTextArea().append(
          username + " removed from channel " + id + " by " + source.getUsername() + " and events sent to users\n");

      // If this channel has no more users, purge it from the server and delete its
      // file. Otherwise, remove the user from the channel file.
      String channelFilePath = "data/channels/" + id;
      if (toDeleteFrom.getUsers().size() <= 0) {
        server.getChannels().remove(id);
        File channelFile = new File(channelFilePath);
        channelFile.delete();
      } else {
        server.getFileWriteQueue().add(new FileWriteEvent(toDeleteFrom, channelFilePath));
      }

      // Remove this channel from the user's file.
      String userFilePath = "data/users/" + toDelete.getUsername();
      server.getFileWriteQueue().add(new FileWriteEvent(toDelete, userFilePath));

    } catch (IOException e) {
      e.printStackTrace();
    }

  }
  
}
