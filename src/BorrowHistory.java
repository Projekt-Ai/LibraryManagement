import javax.swing.*;
import java.awt.*;
import java.sql.*;

/**
 * Panel for viewing borrowing history of library members.
 * Admins can input any member ID. Regular users see their own history.
 */
public class BorrowHistory extends JPanel {
	private static final long serialVersionUID = 1L;

    /**
     * Constructs the BorrowHistory panel.
     * 
     * @param conn The SQL connection to the database.
     * @param cardLayout The layout manager to switch views.
     * @param main The main container panel.
     * @param isAdmin Whether the user is an admin.
     * @param memberId The ID of the current member.
     */
    public BorrowHistory(Connection conn, CardLayout cardLayout, JPanel main, boolean isAdmin, int memberId) {
        setLayout(new BorderLayout(10,10));
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JLabel memberLabel = new JLabel("Member ID:");
        JTextField memberField = new JTextField(10);

        if (isAdmin) {
            top.add(memberLabel);
            top.add(memberField);
        } else {
            top.add(new JLabel("Your ID: " + memberId));
        }

        JButton findButton = new JButton("View History");
        JButton backButton = new JButton("Back");
        top.add(findButton);
        top.add(backButton);
        add(top, BorderLayout.NORTH);

        JTextArea textArea = new JTextArea(15, 60);
        textArea.setEditable(false);
        add(new JScrollPane(textArea), BorderLayout.CENTER);

        /**
         * Handles the "View History" button click.
         * Queries the borrow history and updates the display.
         */
        findButton.addActionListener(e -> {
            int searchMemberId;
            if (isAdmin) {
                try {
                    searchMemberId = Integer.parseInt(memberField.getText().trim());
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Please enter a valid numeric Member ID.");
                    return;
                }
            } else {
                searchMemberId = memberId;
            }

            String sql =
              "SELECT b.bookid, bk.title, b.borrowdate, b.duedate, b.returndate " +
              "FROM Borrowings b " +
              "JOIN Books bk ON b.bookid = bk.bookid " +
              "WHERE b.memberid = ?";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, searchMemberId);
                ResultSet rs = ps.executeQuery();

                StringBuilder sb = new StringBuilder();
                while (rs.next()) {
                    sb.append("Book ID: ").append(rs.getInt("bookid"))
                      .append(", Title: ").append(rs.getString("title"))
                      .append(", Borrowed: ").append(rs.getDate("borrowdate"))
                      .append(", Due: ").append(rs.getDate("duedate"))
                      .append(", Returned: ");

                    Date returnDate = rs.getDate("returndate");
                    sb.append(returnDate != null ? returnDate.toString() : "Not returned")
                      .append("\n");
                }
                textArea.setText(sb.length() > 0 ? sb.toString() : "No records found.");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        /**
         * Handles the "Back" button click to return to the home screen.
         */
        backButton.addActionListener(e -> cardLayout.show(main, "home"));
    }
}
