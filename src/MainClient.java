import duberchat.client.ChatClient;

/**
 * This class is the main class and launches the main client.
 */
public class MainClient {
    public static void main(String[] args) {
        ChatClient chatClient = new ChatClient();
        chatClient.start();
    }
}
