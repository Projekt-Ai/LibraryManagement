import javax.swing.*;
import java.awt.*;

/**
 * AdminPanel handles login for administrator users.
 * It provides fields for username and password, and triggers a login listener upon successful authentication.
 */
public class AdminPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private LoginListener listener;

    /**
     * Constructs the admin login panel with username and password fields.
     *
     * @param listener the LoginListener to be notified when login is successful
     */
    public AdminPanel(LoginListener listener) {
        this.listener = listener;
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder("Admin Login"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        usernameField = new JTextField(15);
        passwordField = new JPasswordField(15);
        JButton loginButton = new JButton("Login");

        loginButton.addActionListener(e -> authenticate());

        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        add(passwordField, gbc);

        gbc.gridx = 1; gbc.gridy = 2; gbc.anchor = GridBagConstraints.EAST;
        add(loginButton, gbc);
    }

    /**
     * Authenticates the admin user based on fixed credentials ("Admin"/"pass").
     * If successful, notifies the LoginListener. Otherwise, shows an error dialog.
     */
    private void authenticate() {
        String user = usernameField.getText().trim();
        String pass = new String(passwordField.getPassword()).trim();

        if (user.equals("Admin") && pass.equals("pass")) {
            listener.onLoginSuccess(user, true, 0);
        } else {
            JOptionPane.showMessageDialog(this, "Invalid admin credentials.");
        }
    }
}
