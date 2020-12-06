package duberchat.handlers.server;

import java.io.*;
import java.util.HashMap;

import duberchat.chatutil.Channel;
import duberchat.chatutil.User;
import duberchat.events.AuthFailedEvent;
import duberchat.events.AuthSucceedEvent;
import duberchat.events.ClientLoginEvent;
import duberchat.events.SerializableEvent;
import duberchat.handlers.Handleable;
import duberchat.server.ChatServer;
import duberchat.client.ChatClient;

public class ClientLoginHandler implements Handleable {
  private ObjectOutputStream output;
  private User associatedUser;
  private HashMap<Integer, Channel> serverChannels;
  private HashMap<String, User> serverUsers;

  public ClientLoginHandler(HashMap<Integer, Channel> serverChannels, 
                            HashMap<String, User> serverUsers,
                            User associatedUser, ObjectOutputStream output) {
    this.serverChannels = serverChannels;
    this.serverUsers = serverUsers;
    this.associatedUser = associatedUser;
    this.output = output;
  }
  
  /**
   * For a client login handler, handling the event process client login.
   * <p>
   * It gets back the appropriate user associated with the event and relays such
   * information (or a notification of failure) to said client. 
   */
  public void handleEvent(SerializableEvent newEvent) {
    ClientLoginEvent event = (ClientLoginEvent) newEvent;
    String username = event.getUsername();
    int hashedPassword = event.getHashedPassword();
    File userFile = new File("data/users/" + username + ".txt");

    // Case 1: new user
    if (event.getIsNewUser()) {
      boolean created = false;
      try {
        created = userFile.createNewFile();
        // If the username is already taken, send auth failed event
        if (!created) {
          // TODO: fix null source
          output.writeObject(new AuthFailedEvent(null));
          output.flush();
          return;
        }

        // Create the new user file.
        FileWriter writer = new FileWriter(userFile);
        writer.write(username + "\n");
        writer.write(hashedPassword + "\n");
        writer.write("default.png" + "\n");
        writer.write(0 + "\n");
        writer.close();

        System.out.println("Made new user.");
        this.associatedUser = new User(username);
        serverUsers.put(username, this.associatedUser);
        // TODO: fix null source
        output.writeObject(new AuthSucceedEvent(null, this.associatedUser, new HashMap<Integer, Channel>()));
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
      // skip over username, password, and pfp lines
      // assumption is made that file was titled correctly (aka file title = username)
      for (int i = 0; i < 3; i++) {
        reader.readLine();
      }
      // associatedUser should never be null; if it's null, FileNotFoundException would've been caught
      associatedUser = serverUsers.get(username);
      int numChannels = Integer.parseInt(reader.readLine().trim());
      HashMap<Integer, Channel> userChannels = new HashMap<>();
      for (int i = 0; i < numChannels; i++) {
        int channelId = Integer.parseInt(reader.readLine().trim());
        userChannels.put(channelId, serverChannels.get(channelId));
      }
      // TODO: fix null source 
      output.writeObject(new AuthSucceedEvent(null, associatedUser, userChannels));
      output.flush();
      reader.close();
    } catch (FileNotFoundException e) {
      try {
        // TODO: fix null source
        output.writeObject(new AuthFailedEvent(null));
      } catch (IOException e2) {
        e2.printStackTrace();
      }
    } catch (IOException e1) {
      e1.printStackTrace();
    }
  }

}
