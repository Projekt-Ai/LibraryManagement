import java.awt.*;
import java.sql.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import javax.swing.*;

/**
 * BookReturn panel allows users to return a borrowed book.
 * It records the return date, checks for late returns, and inserts a fine if applicable.
 */
public class BookReturn extends JPanel {
    private static final long serialVersionUID = 1L;
    
    private JTextField memberIdField, bookIdField, returnDateField;
    private static final double DAILY_FINE_RATE = 1.5;
    JPanel main;
    CardLayout cardLayout;

    /**
     * Constructs the BookReturn panel.
     *
     * @param conn        the database connection
     * @param main        the main panel container for switching views
     * @param cardLayout  the layout manager used for view switching
     */
    public BookReturn(Connection conn, JPanel main, CardLayout cardLayout) {
        this.main = main;
        this.cardLayout = cardLayout;

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Member ID input
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel("Member ID:"), gbc);

        memberIdField = new JTextField(10);
        gbc.gridx = 1;
        add(memberIdField, gbc);

        // Book ID input
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(new JLabel("Book ID:"), gbc);

        bookIdField = new JTextField(10);
        gbc.gridx = 1;
        add(bookIdField, gbc);

        // Return Date input
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(new JLabel("Return Date:"), gbc);

        returnDateField = new JTextField(LocalDate.now().toString(), 10);
        gbc.gridx = 1;
        add(returnDateField, gbc);

        // Buttons
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton returnButton = new JButton("Return Book");
        JButton backButton = new JButton("Back");

        buttonPanel.add(returnButton);
        buttonPanel.add(backButton);
        add(buttonPanel, gbc);

        // Return button logic
        returnButton.addActionListener(e -> {
            try {
                int memberId = Integer.parseInt(memberIdField.getText().trim());
                int bookId = Integer.parseInt(bookIdField.getText().trim());
                LocalDate returnDate = LocalDate.parse(returnDateField.getText().trim());

                returnBook(conn, memberId, bookId, returnDate);
                JOptionPane.showMessageDialog(this, "Book returned successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                cardLayout.show(main, "home");
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Back button logic
        backButton.addActionListener(e -> cardLayout.show(main, "home"));
    }

    /**
     * Updates the return date in the database and inserts a fine if the return is late.
     *
     * @param conn       the database connection
     * @param memberId   the ID of the member returning the book
     * @param bookId     the ID of the book being returned
     * @param returnDate the date the book is returned
     * @throws SQLException if a database error occurs
     */
    private void returnBook(Connection conn, int memberId, int bookId, LocalDate returnDate) throws SQLException {
        // Update return date
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE Borrowings SET returnDate = ? WHERE memberID = ? AND bookID = ?")) {
            ps.setDate(1, Date.valueOf(returnDate));
            ps.setInt(2, memberId);
            ps.setInt(3, bookId);
            ps.executeUpdate();
        }

        LocalDate borrowDate, dueDate;

        // Retrieve borrowing record
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT borrowDate, dueDate FROM Borrowings WHERE memberID = ? AND bookID = ?")) {
            ps.setInt(1, memberId);
            ps.setInt(2, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("No borrowing record found for member " + memberId + ", book " + bookId);
                }
                borrowDate = rs.getDate("borrowDate").toLocalDate();
                dueDate = rs.getDate("dueDate").toLocalDate();
            }
        }

        // Calculate fine if overdue
        long daysLate = ChronoUnit.DAYS.between(dueDate, returnDate);
        if (daysLate > 0) {
            double amount = daysLate * DAILY_FINE_RATE;
            insertFine(conn, memberId, bookId, borrowDate, amount);
        }
    }

    /**
     * Inserts a new fine record into the database.
     *
     * @param conn        the database connection
     * @param memberId    the ID of the member being fined
     * @param bookId      the ID of the overdue book
     * @param borrowDate  the original borrow date
     * @param amount      the calculated fine amount
     * @throws SQLException if a database error occurs
     */
    private void insertFine(Connection conn, int memberId, int bookId, LocalDate borrowDate, double amount) throws SQLException {
        int nextFineId = 1;

        // Get next available fine ID
        try (PreparedStatement ps = conn.prepareStatement("SELECT MAX(fineID) FROM Fines");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next() && rs.getInt(1) != 0) {
                nextFineId = rs.getInt(1) + 1;
            }
        }

        // Insert fine
        String ins = "INSERT INTO Fines (fineID, memberID, bookID, borrowDate, amount, paidStatus, paymentDueDate) "
                   + "VALUES (?, ?, ?, ?, ?, FALSE, ?)";

        try (PreparedStatement ps = conn.prepareStatement(ins)) {
            ps.setInt(1, nextFineId);
            ps.setInt(2, memberId);
            ps.setInt(3, bookId);
            ps.setDate(4, Date.valueOf(borrowDate));
            ps.setDouble(5, amount);
            ps.setDate(6, Date.valueOf(LocalDate.now().plusDays(30)));
            ps.executeUpdate();
        }
    }
}
