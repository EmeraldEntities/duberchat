import duberchat.client.ChatClient;

import java.util.*;
import java.io.*;

public class MainClient {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        ChatClient chatClient = new ChatClient();
        chatClient.start();
    }
}
