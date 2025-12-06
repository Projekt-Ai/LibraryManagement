import javax.swing.*;
import java.awt.*;
import java.sql.*;

/**
 * A JPanel that provides a login interface for members.
 * Allows input of member ID and email and performs authentication against a database.
 */
public class MemberPanel extends JPanel {
	private static final long serialVersionUID = 1L;

    private JTextField idField, emailField;
    private LoginListener listener;

    /**
     * Constructs a MemberPanel with the specified LoginListener.
     *
     * @param listener the LoginListener to notify upon successful login
     */
    public MemberPanel(LoginListener listener) {
        this.listener = listener;
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder("Member Login"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        idField = new JTextField(15);
        emailField = new JTextField(15);
        JButton loginButton = new JButton("Login");

        loginButton.addActionListener(e -> authenticate());

        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel("Member ID:"), gbc);

        gbc.gridx = 1;
        add(idField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        add(new JLabel("Email:"), gbc);

        gbc.gridx = 1;
        add(emailField, gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;

        add(loginButton, gbc);
    }

    /**
     * Authenticates the member using input fields by querying the database.
     * On success, notifies the LoginListener; on failure, shows an error dialog.
     */
    private void authenticate() {
        String id = idField.getText().trim();
        String email = emailField.getText().trim();

        if (id.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields required.");
            return;
        }

        try {
            int memberId = Integer.parseInt(id);
            Connection conn = DriverManager.getConnection(SQLogin.URL, SQLogin.USERNAME, SQLogin.PASSWORD);
            String sql = "SELECT * FROM Members WHERE memberId = ? AND email = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, memberId);
            stmt.setString(2, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                listener.onLoginSuccess(rs.getString("fullName"), false, memberId);
            } else {
                JOptionPane.showMessageDialog(this, "Invalid member.");
            }

            conn.close();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "ID must be numeric.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
        }
    }
}
