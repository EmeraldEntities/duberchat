package duberchat.events;

/**
 * A {@code ClientLoginEvent} is an event that is created when a client
 * initially logs in to the server.
 * <p>
 * This event represents both registration and logging in, as they are both
 * forms of logging in to the server.
 * <p>
 * This event specifically exists to contain the additional metadata required
 * for a log-in (ie. username and password). However, log-outs have no such
 * constraints and thus can be represented via a
 * {@link duberchat.events.ClientStatusUpdateEvent ClientStatusUpdateEvent} by
 * simply setting the user to offline.
 * <p>
 * This event is intended to work alongside the
 * {@link duberchat.events.AuthFailedEvent AuthFailed} or
 * {@link duberchat.events.AuthSucceedEvent AuthSuceed} events depending on
 * authentication results.
 * 
 * <p>
 * Since <b>2020-12-04</b>.
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Joseph Wang
 */
public class ClientLoginEvent extends ClientEvent {
    static final long serialVersionUID = 1L;

    protected boolean isNewUser;
    protected String username;
    protected int hashedPassword;

    /**
     * Constructs a new {@code ClientLoginEvent}.
     * 
     * @param source    The source of this event.
     * @param isNewUser Whether the client is logging into an existing user or
     *                  creating a new user.
     * @param username  The username to be used.
     * @param password  The plaintext password to be used. This will be stored
     *                  hashed.
     */
    public ClientLoginEvent(Object source, boolean isNewUser, String username, String password) {
        super(source);

        this.isNewUser = isNewUser;
        this.username = username;
        this.hashedPassword = password.hashCode();
    }

    /**
     * Retrieves whether the client is logging into an existing user or creating a
     * new user.
     * 
     * @return true if the client is creating a new user, false otherwise.
     */
    public boolean getIsNewUser() {
        return this.isNewUser;
    }

    /**
     * Retrieves the username to use as log-in or registration.
     * 
     * @return a String with the username.
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * Retrieves the hashed password to use for log-in or registration.
     * 
     * @return an int with the hashed password.
     */
    public int getHashedPassword() {
        return this.hashedPassword;
    }
}
