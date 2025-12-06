import java.awt.*;
import javax.swing.*;

/**
 * The LoginFrame class provides a GUI for user login, allowing switching between
 * Member and Admin panels. Implements LoginListener to handle login success.
 */
public class LoginFrame extends JFrame implements LoginListener {
	private static final long serialVersionUID = 1L;
    private JPanel cards;
    private CardLayout cardLayout;

    /**
     * Constructs the login frame, initializes UI components, and displays the window.
     */
    public LoginFrame() {
        setTitle("Library Login");
        setSize(1024, 768);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        
        cards = new JPanel(cardLayout);
        cards.add(new MemberPanel(this), "Member");
        cards.add(new AdminPanel(this),  "Admin");

        JPanel switchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        
        JButton toMember = new JButton("Switch to Member");
        JButton toAdmin  = new JButton("Switch to Admin");
        
        toMember.setPreferredSize(new Dimension(180, 30));
        toAdmin .setPreferredSize(new Dimension(180, 30));
        toMember.addActionListener(e -> cardLayout.show(cards, "Member"));
        toAdmin .addActionListener(e -> cardLayout.show(cards, "Admin"));
        
        switchPanel.add(toMember);
        switchPanel.add(toAdmin);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(cards, BorderLayout.CENTER);
        getContentPane().add(switchPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    /**
     * Callback method invoked upon successful login.
     *
     * @param name     the name of the logged-in user
     * @param isAdmin  true if the user is an admin, false otherwise
     * @param memberId the ID of the member
     */
    @Override
    public void onLoginSuccess(String name, boolean isAdmin, int memberId) {
        JOptionPane.showMessageDialog(this, (isAdmin ? "Admin" : "Member") + " login successful!");
        dispose();
        new LibraryManager(name, isAdmin, memberId);
    }

    /**
     * The main method to launch the LoginFrame.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame());
    }
}
