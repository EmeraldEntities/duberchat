package duberchat.handlers.server;

import duberchat.events.MessageDeleteEvent;
import duberchat.events.SerializableEvent;
import duberchat.handlers.Handleable;
import duberchat.server.ChatServer;
import duberchat.chatutil.*;

import java.util.HashMap;
import java.io.*;

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

    String filePath = "data/channels/" + toDeleteFrom.getChannelId() + ".txt";
    File channelFile = new File(filePath);
    try {
      // delete the message from its channel file
      BufferedReader fileReader = new BufferedReader(new FileReader(channelFile));
      HashMap<String, HashMap<Integer, String>> rewriteMap = new HashMap<>();
      HashMap<Integer, String> rewriteInnerMap = new HashMap<>();
      int lineNum = 5 + toDeleteFrom.getAdminUsers().size() + toDeleteFrom.getUsers().size();
      rewriteInnerMap.put(lineNum, toDeleteFrom.getMessages().size() + "\n");
      // skip the lines we don't care about
      for (int i = 0; i <= lineNum; i++) {
        fileReader.readLine();
      }
      int curId = -1;
      while (curId != toDelete.getMessageId()) {
        lineNum++;
        curId = Integer.parseInt(fileReader.readLine().trim().split(" ")[0]);
      }
      fileReader.close();
      rewriteInnerMap.put(lineNum, "");
      rewriteMap.put(filePath, rewriteInnerMap);
      server.getFileRewriteQueue().add(rewriteMap);

      // give a message deletion event to all online users in the channel
      for (User user : toDeleteFrom.getUsers()) {
        if (!server.getCurUsers().containsKey(user)) continue;
        ObjectOutputStream output = server.getCurUsers().get(user).getOutputStream();
        output.writeObject(new MessageDeleteEvent((User) event.getSource(), serverToDelete));
        output.flush();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
}
