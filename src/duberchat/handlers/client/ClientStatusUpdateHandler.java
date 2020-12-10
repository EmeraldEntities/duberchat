package duberchat.handlers.client;

import duberchat.chatutil.Channel;
import duberchat.chatutil.User;
import duberchat.client.ChatClient;
import duberchat.events.ClientStatusUpdateEvent;
import duberchat.events.SerializableEvent;
import duberchat.handlers.Handleable;

public class ClientStatusUpdateHandler implements Handleable {
    protected ChatClient client;

    public ClientStatusUpdateHandler(ChatClient client) {
        this.client = client;
    }

    public void handleEvent(SerializableEvent event) {
        ClientStatusUpdateEvent statusEvent = (ClientStatusUpdateEvent) event;
        User user = (User) statusEvent.getSource();

        // If i initialized it, the source would be from me
        if (this.client.getUser().equals(user)) {
            this.client.getUser().setStatus(statusEvent.getStatus());
        } else {
            for (Channel c : this.client.getChannels().values()) {
                int index = c.getUsers().indexOf(user);
                // Note: this will not change adminUsers user, but we should not be using
                // adminUsers for anything other than checking if someone is admin.

                if (index != -1) {
                    c.getUsers().get(index).setStatus(user.getStatus());
                }
            }
        }

        if (this.client.hasCurrentChannel() && this.client.getCurrentChannel().getUsers().contains(user)) {
            this.client.getMainMenuFrame().reload(event);
        }
    }
}
