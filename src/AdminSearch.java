import javax.swing.*;
import java.awt.*;
import java.sql.*;

/**
 * AdminSearch is a JPanel that allows admin users to search for books in the library database.
 * It supports searching by title, author, and genre, viewing all books, and navigating back.
 */
public class AdminSearch extends JPanel {
    private static final long serialVersionUID = 1L;

    /**
     * Constructs the AdminSearch panel with input fields, result area, and control buttons.
     *
     * @param conn        the active database connection
     * @param cardLayout  the layout manager to switch views
     * @param main        the main container panel used for view switching
     */
    public AdminSearch(Connection conn, CardLayout cardLayout, JPanel main) {
        setLayout(new BorderLayout());

        // Input panel for search criteria
        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField titleField = new JTextField();
        JTextField authorField = new JTextField();
        JTextField genreField = new JTextField();

        inputPanel.add(new JLabel("Title:"));
        inputPanel.add(titleField);
        inputPanel.add(new JLabel("Author:"));
        inputPanel.add(authorField);
        inputPanel.add(new JLabel("Genre:"));
        inputPanel.add(genreField);

        add(inputPanel, BorderLayout.NORTH);

        // Area to display search results
        JTextArea resultArea = new JTextArea(15, 60);
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);
        add(scrollPane, BorderLayout.CENTER);

        // Button panel with Search, View All, and Back buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton searchButton = new JButton("Search");
        JButton viewAll = new JButton("View All");
        JButton backButton = new JButton("Back");

        searchButton.setPreferredSize(new Dimension(100, 30));
        viewAll.setPreferredSize(new Dimension(100, 30));
        backButton.setPreferredSize(new Dimension(100, 30));

        buttonPanel.add(searchButton);
        buttonPanel.add(viewAll);
        buttonPanel.add(backButton);
        add(buttonPanel, BorderLayout.SOUTH);

        /**
         * Executes a search query based on user input and displays the results in the resultArea.
         */
        searchButton.addActionListener(e -> {
            String title = titleField.getText().trim();
            String author = authorField.getText().trim();
            String genre = genreField.getText().trim();

            try {
                String query = """
                    SELECT b.bookid, b.title, a.fullname AS author, b.genre, 
                           b.publicationyear, b.isbn
                    FROM books b
                    JOIN authors a ON b.authorid = a.authorid
                    WHERE LOWER(b.title) LIKE ?
                      AND LOWER(a.fullname) LIKE ?
                      AND LOWER(b.genre) LIKE ?
                    """;
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, "%" + title.toLowerCase() + "%");
                stmt.setString(2, "%" + author.toLowerCase() + "%");
                stmt.setString(3, "%" + genre.toLowerCase() + "%");

                ResultSet rs = stmt.executeQuery();
                StringBuilder sb = new StringBuilder();
                while (rs.next()) {
                    sb.append("ID: ").append(rs.getInt("bookid"))
                      .append(", Title: ").append(rs.getString("title"))
                      .append(", Author: ").append(rs.getString("author"))
                      .append(", Genre: ").append(rs.getString("genre"))
                      .append(", Year: ").append(rs.getInt("publicationyear"))
                      .append(", ISBN: ").append(rs.getString("isbn"))
                      .append("\n");
                }

                resultArea.setText(sb.length() > 0 ? sb.toString() : "No results found.");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Search error: " + ex.getMessage());
            }
        });

        /**
         * Displays all books from the database in the resultArea.
         */
        viewAll.addActionListener(e -> {
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM books")) {
                StringBuilder sb = new StringBuilder();
                while (rs.next()) {
                    sb.append("ID: ").append(rs.getInt("bookid"))
                      .append(", Title: ").append(rs.getString("title"))
                      .append(", Author: ").append(rs.getString("authorid"))
                      .append(", Year: ").append(rs.getInt("publicationyear"))
                      .append(", ISBN: ").append(rs.getString("isbn"))
                      .append("\n");
                }
                resultArea.setText(sb.toString());
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error retrieving books: " + ex.getMessage());
            }
        });

        /**
         * Switches back to the home panel.
         */
        backButton.addActionListener(e -> cardLayout.show(main, "home"));
    }
}
