import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.time.LocalDate;

/**
 * A JPanel that displays a searchable library catalogue.
 * Users can search for books by title, author, or genre and borrow available books.
 */
public class LibraryCatalogue extends JPanel {
    private static final long serialVersionUID = 1L;

    private Connection conn;
    private CardLayout cardLayout;
    private JPanel main;
    private int currentMemberId;

    private JTextField titleField, authorField, genreField;
    private JTable resultsTable;
    private DefaultTableModel tableModel;
    private JButton borrowButton, backButton;

    /**
     * Constructs the LibraryCatalogue panel.
     *
     * @param conn       the database connection
     * @param cardLayout the layout manager to control view switching
     * @param main       the main container panel
     * @param memberId   the ID of the current logged-in member
     */
    public LibraryCatalogue(Connection conn, CardLayout cardLayout, JPanel main, int memberId) {
        this.conn = conn;
        this.cardLayout = cardLayout;
        this.main = main;
        this.currentMemberId = memberId;
        initComponents();
    }

    /**
     * Initializes all UI components and sets up layout and listeners.
     */
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        JPanel searchBar = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        titleField = new JTextField(10);
        authorField = new JTextField(10);
        genreField = new JTextField(10);
        JButton searchButton = new JButton("Search");

        gbc.gridx = 0; gbc.gridy = 0;
        searchBar.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1;
        searchBar.add(titleField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        searchBar.add(new JLabel("Author:"), gbc);
        gbc.gridx = 1;
        searchBar.add(authorField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        searchBar.add(new JLabel("Genre:"), gbc);
        gbc.gridx = 1;
        searchBar.add(genreField, gbc);

        gbc.gridx = 1; gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        searchBar.add(searchButton, gbc);

        add(searchBar, BorderLayout.NORTH);

        String[] cols = {"Book ID", "Title", "Author", "Genre", "Year", "Copies"};
        tableModel = new DefaultTableModel(cols, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        resultsTable = new JTable(tableModel);
        add(new JScrollPane(resultsTable), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        borrowButton = new JButton("Borrow Selected");
        backButton = new JButton("Back");
        buttonPanel.add(borrowButton);
        buttonPanel.add(backButton);
        add(buttonPanel, BorderLayout.SOUTH);

        searchButton.addActionListener(e -> runSearch());
        borrowButton.addActionListener(e -> borrowSelected());
        backButton.addActionListener(e -> cardLayout.show(main, "home"));
    }

    /**
     * Executes a search based on title, author, and genre inputs.
     * Populates the results table with matching books.
     */
    private void runSearch() {
        String sql = """
            SELECT b.bookID, b.title, a.fullName AS author, b.genre,
                   b.publicationYear, b.availableCopies
            FROM Books b
            JOIN Authors a ON b.authorID = a.authorID
            WHERE LOWER(b.title) LIKE ? AND LOWER(a.fullName) LIKE ? AND LOWER(b.genre) LIKE ?
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + titleField.getText().toLowerCase() + "%");
            ps.setString(2, "%" + authorField.getText().toLowerCase() + "%");
            ps.setString(3, "%" + genreField.getText().toLowerCase() + "%");

            ResultSet rs = ps.executeQuery();
            tableModel.setRowCount(0); // clear previous results

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("bookID"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("genre"),
                    rs.getInt("publicationYear"),
                    rs.getInt("availableCopies")
                });
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Search error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Handles the borrowing of the selected book in the results table.
     * Updates the database and shows confirmation or error messages.
     */
    private void borrowSelected() {
        int row = resultsTable.getSelectedRow();

        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                "Please select a book first.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int bookId = (int) tableModel.getValueAt(row, 0);
        int copies = (int) tableModel.getValueAt(row, 5);

        if (copies < 1) {
            JOptionPane.showMessageDialog(this,
                "Selected Book is unavailable.", "Unavailable", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDate today = LocalDate.now();
        LocalDate dueDate = today.plusWeeks(10); // ~2.5 months

        try {
            String dup = "SELECT 1 FROM Borrowings WHERE memberID = ? AND bookID = ? AND returnDate IS NULL";
            try (PreparedStatement ps = conn.prepareStatement(dup)) {
                ps.setInt(1, currentMemberId);
                ps.setInt(2, bookId);
                if (ps.executeQuery().next()) {
                    JOptionPane.showMessageDialog(this,
                        "Already borrowed and not returned.", "Duplicate", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            String ins = "INSERT INTO Borrowings(memberID, bookID, borrowDate, dueDate, returnDate) VALUES (?, ?, ?, ?, NULL)";
            try (PreparedStatement ps = conn.prepareStatement(ins)) {
                ps.setInt(1, currentMemberId);
                ps.setInt(2, bookId);
                ps.setDate(3, Date.valueOf(today));
                ps.setDate(4, Date.valueOf(dueDate));
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement("UPDATE Books SET availableCopies = availableCopies - 1 WHERE bookID = ?")) {
                ps.setInt(1, bookId);
                ps.executeUpdate();
            }

            JOptionPane.showMessageDialog(this,
                "Borrowed until " + dueDate + "!", "Success", JOptionPane.INFORMATION_MESSAGE);

            runSearch(); // refresh available copies in table

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error borrowing: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}
