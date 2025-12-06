/**
 * Listener interface for handling successful login events.
 * Implement this interface to define what happens after a user logs in.
 */
public interface LoginListener {
    /**
     * Called when a login attempt is successful.
     *
     * @param name     the name of the logged-in user
     * @param isAdmin  true if the user is an administrator, false otherwise
     * @param memberId the ID of the logged-in member
     */
    void onLoginSuccess(String name, boolean isAdmin, int memberId);
}
