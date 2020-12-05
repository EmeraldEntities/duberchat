package duberchat.handlers.server;

import java.io.*;
import java.util.HashMap;

import duberchat.chatutil.Channel;
import duberchat.chatutil.User;
import duberchat.events.AuthFailedEvent;
import duberchat.events.AuthSucceedEvent;
import duberchat.events.ClientLoginEvent;
import duberchat.handlers.Handleable;
import duberchat.server.ChatServer;

public class ClientLoginHandler implements Handleable {
  private ClientLoginEvent event;
  private ChatServer server;
  private ObjectOutputStream output;
  private User associatedUser;

  public ClientLoginHandler(ClientLoginEvent event, ChatServer server, ObjectOutputStream output,
                            User associatedUser) {
    this.event = event;
    this.server = server;
    this.output = output;
    this.associatedUser = associatedUser;
  }
  
  /**
   * For a client login handler, handling the event process client login.
   * <p>
   * It gets back the appropriate user associated with the event and relays such
   * information (or a notification of failure) to said client. 
   */
  public void handleEvent() {
    String username = event.getUsername();
    int hashedPassword = event.getHashedPassword();
    File userFile = new File("users/" + username + ".txt");

    // Case 1: new user
    if (event.getIsNewUser()) {
      boolean created = false;
      try {
        created = userFile.createNewFile();
        // If the username is already taken, send auth failed event
        if (!created) {
          output.writeObject(new AuthFailedEvent(server));
          return;
        }

        // Create the new user file.
        FileWriter writer = new FileWriter(userFile);
        int numUsers = server.getNumUsers();
        writer.write(numUsers + "\n");
        writer.write(username + "\n");
        writer.write(hashedPassword + "\n");
        writer.write("default.png" + "\n");
        writer.write(0 + "\n");
        writer.close();

        System.out.println("Made new user.");
        this.associatedUser = new User(username, numUsers);
        output.writeObject(new AuthSucceedEvent(server, this.associatedUser, new HashMap<Integer, Channel>()));
        output.flush();

        System.out.println("SYSTEM: Sent auth event.");
      } catch (IOException e) {
        e.printStackTrace();
      }

      return;
    }

    // Case 2: already registered user
    try {
      BufferedReader reader = new BufferedReader(new FileReader(userFile));
      int userId = Integer.parseInt(reader.readLine().trim());
      // skip over username and password lines
      // assumption is made that file was titled correctly (aka file title = username)
      reader.readLine();
      reader.readLine();
      String pfpPath = reader.readLine().trim();
      associatedUser = new User(username, userId, pfpPath);
      int numChannels = Integer.parseInt(reader.readLine().trim());
      HashMap<Integer, Channel> userChannels = new HashMap<>();
      for (int i = 0; i < numChannels; i++) {
        int channelId = Integer.parseInt(reader.readLine().trim());
        userChannels.put(channelId, this.server.getChannels().get(channelId));
      }
      output.writeObject(new AuthSucceedEvent(this.server, associatedUser, userChannels));
      reader.close();
    } catch (FileNotFoundException e) {
      try {
        output.writeObject(new AuthFailedEvent(this.server));
      } catch (IOException e2) {
        e2.printStackTrace();
      }
    } catch (IOException e1) {
      e1.printStackTrace();
    }
  }

}
