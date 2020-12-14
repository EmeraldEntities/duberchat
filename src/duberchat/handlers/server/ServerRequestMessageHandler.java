package duberchat.handlers.server;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import duberchat.chatutil.*;
import duberchat.events.ClientRequestMessageEvent;
import duberchat.events.RequestFailedEvent;
import duberchat.events.SerializableEvent;
import duberchat.handlers.Handleable;
import duberchat.server.ChatServer;

public class ServerRequestMessageHandler implements Handleable {
  private ChatServer server;

  public ServerRequestMessageHandler(ChatServer server) {
    this.server = server;
  }

  public void handleEvent(SerializableEvent newEvent) {
    ClientRequestMessageEvent event = (ClientRequestMessageEvent) newEvent;
    User source = server.getAllUsers().get(((User) event.getSource()).getUsername());
    ObjectOutputStream output = server.getCurUsers().get(source).getOutputStream();

    Message lastMessage = event.getStartMsg();
    Channel serverChannel = server.getChannels().get(event.getChannel().getChannelId());
    ArrayList<Message> messages = serverChannel.getMessages();
    ArrayList<Message> messageBlock = new ArrayList<>();

    // loop through messages to find the starting section.
    for (int i = messages.size() - 1; i >= 0; i--) {
      Message curMessage = messages.get(i);

      // remember, new messages go at the end, so old messages go to the top
      messageBlock.add(0, curMessage);
      if (curMessage.equals(lastMessage)) {
        Message startMsg = curMessage;
        // Add the next thirty messages to the message block
        for (int j = i - 1; j >= Math.max(i - 30, 0); j--) {
          messageBlock.add(0, messages.get(j));
        }

        try {
          Channel clientVer = new Channel(serverChannel);
          clientVer.setMessages(messageBlock);
          clientVer.setMessageClusters(clientVer.getMessageClusters() + 1);
          output.writeObject(new ClientRequestMessageEvent(source, startMsg, clientVer));
          output.flush();
        } catch (IOException e) {
          e.printStackTrace();
        }
        server.getServerFrame().getTextArea()
            .append(source.getUsername() + " requested messages. Messages found, event sent to user.\n");
        return;
      }
    }

    // Send back a request failed if the message can't be found or the bounds are illegal.
    try {
      output.writeObject(new RequestFailedEvent(source));
      output.flush();
      server.getServerFrame().getTextArea()
          .append(source.getUsername() + " requested messages. Request failed, sent event to users.\n");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
}
