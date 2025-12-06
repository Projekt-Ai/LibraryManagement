import java.awt.*;
import java.sql.*;
import javax.swing.*;
import java.text.NumberFormat;

/**
 * JPanel allowing a member to view outstanding fines and make payments.
 */
public class Payment extends JPanel {
    private static final long serialVersionUID = 1L;
    Connection conn;
    private int memberId;
    private JLabel fineLabel;

    /**
     * Constructs the payment panel.
     * 
     * @param conn       Database connection object (may be reassigned internally)
     * @param main       Parent panel container with CardLayout
     * @param cardLayout CardLayout controlling panel switching
     * @param memberId   ID of the member making payments
     */
    public Payment(Connection conn, JPanel main, CardLayout cardLayout, int memberId) {
        this.memberId = memberId;

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new java.awt.Insets(5,5,5,5);
        gbc.anchor = GridBagConstraints.WEST;

        double fineValue = fetchOutstandingFine();
        fineLabel = new JLabel(NumberFormat.getCurrencyInstance().format(fineValue));

        add(new JLabel("Outstanding Fines:"), gbc);
        gbc.gridx = 1;
        add(fineLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        add(new JLabel("Payment Method:"), gbc);
        
        String[] methods = {"Cash", "Card"};
        JComboBox<String> methodBox = new JComboBox<>(methods);
        
        gbc.gridx = 1;
        add(methodBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        add(new JLabel("Amount to Pay:"), gbc);
        
        JTextField amountField = new JTextField(10);
        
        gbc.gridx = 1;
        add(amountField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        add(buttonPanel, gbc);

        JButton pay = new JButton("Submit Payment");
        JButton backButton = new JButton("Back");
        
        buttonPanel.add(pay);
        buttonPanel.add(backButton);

        pay.addActionListener(e -> {
            try {
                double entered = Double.parseDouble(amountField.getText().trim());
                if (entered <= 0 || entered > fineValue) {
                    JOptionPane.showMessageDialog(this,
                        "Enter an amount between $0.01 and " + NumberFormat.getCurrencyInstance().format(fineValue),
                        "Invalid Amount", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String method = (String) methodBox.getSelectedItem();
                int confirm = JOptionPane.showConfirmDialog(this,
                    "Pay " + NumberFormat.getCurrencyInstance().format(entered) +
                    " by " + method + "?", "Confirm Payment",
                    JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    processPayment(entered, method, fineValue);
                    
                    double newFine = fetchOutstandingFine();
                    fineLabel.setText(NumberFormat.getCurrencyInstance().format(newFine));

                    JOptionPane.showMessageDialog(this,
                        "Payment successful!", "Done", JOptionPane.INFORMATION_MESSAGE);

                    cardLayout.show(main, "home");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                    "Please enter a valid numeric amount.",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        backButton.addActionListener(e ->
            cardLayout.show(main, "home"));
    }

    /**
     * Fetches the total outstanding fine amount for the current member.
     * 
     * @return Total outstanding fine amount
     */
    private double fetchOutstandingFine() {
        double total = 0;
        try {
            conn = DriverManager.getConnection(SQLogin.URL, SQLogin.USERNAME, SQLogin.PASSWORD);
            PreparedStatement ps = conn.prepareStatement("SELECT SUM(amount) FROM Fines WHERE memberID=? AND paidStatus=FALSE");
            ps.setInt(1, memberId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) total = rs.getDouble(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return total;
    }

    /**
     * Generates the next payment ID for inserting into the Payments table.
     * 
     * @param conn Active database connection
     * @return Next payment ID
     * @throws SQLException if query fails
     */
    private int getNextPaymentID(Connection conn) throws SQLException {
        String sql = "SELECT COALESCE(MAX(paymentID), 0) + 1 FROM Payments";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs   = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    /**
     * Processes the payment by applying the amount to outstanding fines,
     * updating records accordingly.
     * 
     * @param paid        The amount paid
     * @param method      Payment method (e.g., Cash, Card)
     * @param outstanding The total outstanding fine before payment
     */
    private void processPayment(double paid, String method, double outstanding) {
        try {
            conn = DriverManager.getConnection(SQLogin.URL, SQLogin.USERNAME, SQLogin.PASSWORD);

            String fetchFines = "SELECT fineID, amount FROM Fines " +
                "WHERE memberID=? AND paidStatus=FALSE " +
                "ORDER BY borrowDate";

            try (PreparedStatement ps = conn.prepareStatement(fetchFines)) {
                ps.setInt(1, memberId);
                try (ResultSet rs = ps.executeQuery()) {
                    double remaining = paid;
                    while (rs.next() && remaining > 0) {
                        int fineID   = rs.getInt("fineID");
                        double amt   = rs.getDouble("amount");
                        double toPay = Math.min(amt, remaining);

                        int newPaymentID = getNextPaymentID(conn);

                        try (PreparedStatement ins = conn.prepareStatement(
                                "INSERT INTO Payments(paymentID, fineID, memberID, paymentDate, method, amountPaid) " +
                                "VALUES(?, ?, ?, CURRENT_DATE, ?, ?)")) {
                            ins.setInt(1, newPaymentID);
                            ins.setInt(2, fineID);
                            ins.setInt(3, memberId);
                            ins.setString(4, method);
                            ins.setDouble(5, toPay);
                            ins.executeUpdate();
                        }

                        if (toPay >= amt) {
                            try (PreparedStatement up = conn.prepareStatement(
                                "UPDATE Fines SET paidStatus=TRUE WHERE fineID=?")) {
                                up.setInt(1, fineID);
                                up.executeUpdate();
                            }
                        } else {
                            try (PreparedStatement up = conn.prepareStatement(
                                "UPDATE Fines SET amount = amount - ? WHERE fineID=?")) {
                                up.setDouble(1, toPay);
                                up.setInt(2, fineID);
                                up.executeUpdate();
                            }
                        }
                        remaining -= toPay;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
