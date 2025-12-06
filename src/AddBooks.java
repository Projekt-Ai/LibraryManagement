import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * A JPanel for administrators to add new books to the library database.
 * Allows input of book details such as ID, title, author ID, genre, year, ISBN, and available copies.
 * Includes a "Back" button to return to the home screen.
 * 
 * Assumes that a valid {@link java.sql.Connection} will be supplied and that 
 * table `Books` exists with appropriate columns.
 * 
 * @author 
 */
public class AddBooks extends JPanel {
	private static final long serialVersionUID = 1L;

	/** Connection to the database */
	Connection conn;

	/** Input fields for the book's details */
	private JTextField bookIdField, titleField, authorIdField, genreField, publicationField, isbnField, availableCopiesField;

	/**
	 * Constructs the AddBooks panel.
	 *
	 * @param conn a valid SQL Connection object to the database
	 * @param main the main container panel holding multiple screens
	 * @param cardLayout the layout manager for switching between screens
	 */
	public AddBooks(Connection conn, JPanel main, CardLayout cardLayout) {
		setLayout(new GridBagLayout());

		JPanel inputPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		bookIdField = new JTextField(20);
		titleField = new JTextField(20);
		authorIdField = new JTextField(20);
		genreField = new JTextField(20);
		publicationField = new JTextField(20);
		isbnField = new JTextField(20);
		availableCopiesField = new JTextField(20);

		gbc.gridx = 0; gbc.gridy = 0;
		inputPanel.add(new JLabel("Book ID:"), gbc);
		gbc.gridx = 1;
		inputPanel.add(bookIdField, gbc);

		gbc.gridx = 0; gbc.gridy = 1;
		inputPanel.add(new JLabel("Title:"), gbc);
		gbc.gridx = 1;
		inputPanel.add(titleField, gbc);

		gbc.gridx = 0; gbc.gridy = 2;
		inputPanel.add(new JLabel("Author ID:"), gbc);
		gbc.gridx = 1;
		inputPanel.add(authorIdField, gbc);

		gbc.gridx = 0; gbc.gridy = 3;
		inputPanel.add(new JLabel("Genre:"), gbc);
		gbc.gridx = 1;
		inputPanel.add(genreField, gbc);

		gbc.gridx = 0; gbc.gridy = 4;
		inputPanel.add(new JLabel("Publication Year: "), gbc);
		gbc.gridx = 1;
		inputPanel.add(publicationField, gbc);

		gbc.gridx = 0; gbc.gridy = 5;
		inputPanel.add(new JLabel("ISBN: "), gbc);
		gbc.gridx = 1;
		inputPanel.add(isbnField, gbc);

		gbc.gridx = 0; gbc.gridy = 6;
		inputPanel.add(new JLabel("Available Copies: "), gbc);
		gbc.gridx = 1;
		inputPanel.add(availableCopiesField, gbc);

		add(inputPanel);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
		JButton addBookButton = new JButton("Add Member"); // NOTE: You might want to change this label to "Add Book"
		JButton backButton = new JButton("Back");

		gbc.gridx = 0; gbc.gridy = 6;
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.CENTER;

		add(buttonPanel, gbc);
		buttonPanel.add(addBookButton);
		buttonPanel.add(backButton);

		addBookButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				insertBook();
			}
		});

		backButton.addActionListener(e -> cardLayout.show(main, "home"));
	}

	/**
	 * Inserts the book data from the form fields into the database.
	 * Displays a dialog on success or prints a stack trace on failure.
	 */
	public void insertBook() {
		try {
			int bookId = Integer.parseInt(bookIdField.getText().trim());
			String title = titleField.getText().trim();
			int authorId = Integer.parseInt(authorIdField.getText().trim());
			String genre = genreField.getText().trim();
			int publicationYear = Integer.parseInt(publicationField.getText().trim());
			String isbn = isbnField.getText().trim();
			int availableCopies = Integer.parseInt(availableCopiesField.getText().trim());

			conn = DriverManager.getConnection(SQLogin.URL, SQLogin.USERNAME, SQLogin.PASSWORD);

			String sql = "INSERT INTO Books (bookID, title, authorID, genre, publicationYear, ISBN, availableCopies) " +
			             "VALUES (?, ?, ?, ?, ?, ?, ?)";
			PreparedStatement stmt = conn.prepareStatement(sql);

			stmt.setInt(1, bookId);
			stmt.setString(2, title);
			stmt.setInt(3, authorId);
			stmt.setString(4, genre);
			stmt.setInt(5, publicationYear);
			stmt.setString(6, isbn);
			stmt.setInt(7, availableCopies);

			int rowsInserted = stmt.executeUpdate();

			if (rowsInserted > 0) {
				JOptionPane.showMessageDialog(this, "Book added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
