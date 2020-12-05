import duberchat.server.ChatServer;

public class MainServer {
    /**
     * Main
     * 
     * @param args parameters from command line
     */
    public static void main(String[] args) {
        new ChatServer().go(); // start the server
    }
}
