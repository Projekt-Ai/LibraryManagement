import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

import javax.swing.*;

/**
 * JPanel that provides a form to add new members to the library system.
 * Includes fields for member ID, full name, email, phone number, and join date.
 * Also contains buttons to submit the form or go back to the home screen.
 */
public class AddMember extends JPanel {
	private static final long serialVersionUID = 1L;
	Connection conn;
	private JTextField idField, nameField, emailField, phoneField, dateField;

	/**
	 * Constructs the AddMember panel with input fields and buttons.
	 *
	 * @param conn        The database connection
	 * @param main        The main panel used for card layout navigation
	 * @param cardLayout  The CardLayout used to switch between views
	 */
	public AddMember(Connection conn, JPanel main, CardLayout cardLayout) {
		setLayout(new GridBagLayout());

		JPanel inputPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		idField = new JTextField(20);
		nameField = new JTextField(20);
		emailField = new JTextField(20);
		phoneField = new JTextField(20);
		dateField = new JTextField(20);

		gbc.gridx = 0;
		gbc.gridy = 0;
		inputPanel.add(new JLabel("Member ID:"), gbc);

		gbc.gridx = 1;
		inputPanel.add(idField, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		inputPanel.add(new JLabel("Full Name:"), gbc);

		gbc.gridx = 1;
		inputPanel.add(nameField, gbc);

		gbc.gridx = 0;
		gbc.gridy = 2;
		inputPanel.add(new JLabel("Email:"), gbc);

		gbc.gridx = 1;
		inputPanel.add(emailField, gbc);

		gbc.gridx = 0;
		gbc.gridy = 3;
		inputPanel.add(new JLabel("Phone:"), gbc);

		gbc.gridx = 1;
		inputPanel.add(phoneField, gbc);

		gbc.gridx = 0;
		gbc.gridy = 4;
		inputPanel.add(new JLabel("Join Date:"), gbc);

		gbc.gridx = 1;
		inputPanel.add(dateField, gbc);

		add(inputPanel);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
		JButton addMemberButton = new JButton("Add Member");
		JButton backButton = new JButton("Back");

		gbc.gridx = 0;
		gbc.gridy = 6;
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.CENTER;

		add(buttonPanel, gbc);
		buttonPanel.add(addMemberButton);
		buttonPanel.add(backButton);

		addMemberButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				insertMember();
			}
		});

		backButton.addActionListener(e -> cardLayout.show(main, "home"));
	}

	/**
	 * Inserts a new member into the Members database table using the provided input fields.
	 * Displays a confirmation dialog if the insert is successful.
	 */
	public void insertMember() {
		try {
			int id = Integer.parseInt(idField.getText().trim());
			String name = nameField.getText();
			String email = emailField.getText();
			String phone = phoneField.getText();
			java.sql.Date date = java.sql.Date.valueOf(dateField.getText());

			conn = DriverManager.getConnection(SQLogin.URL, SQLogin.USERNAME, SQLogin.PASSWORD);
			String sql = "INSERT INTO Members (memberID, fullName, email, phone, joinDate, activeStatus) "
					+ "VALUES (?, ?, ?, ?, ?, true)";
			PreparedStatement stmt = conn.prepareStatement(sql);

			stmt.setInt(1, id);
			stmt.setString(2, name);
			stmt.setString(3, email);
			stmt.setString(4, phone);
			stmt.setDate(5, date);

			int rowsInserted = stmt.executeUpdate();

			if (rowsInserted > 0) {
				JOptionPane.showMessageDialog(this, "Member added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
