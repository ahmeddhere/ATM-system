import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class ATM extends JFrame {

    private JLabel lblCardNumber, lblExpirationDate, lblCVC, lblCardholderName;
    private JTextField txtCardNumber, txtExpirationDate, txtCVC, txtCardholderName;
    private JButton btnCheckCard;
    private JPanel panel;

    // Database connection details
    private static final String URL = "jdbc:mysql://localhost:3306/atm management"; // Changed to underscore for consistency
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public ATM() {
        setTitle("Card Information Entry");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Cardholder Name
        lblCardholderName = new JLabel("Cardholder Name:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(lblCardholderName, gbc);

        txtCardholderName = new JTextField(20);
        gbc.gridx = 1;
        panel.add(txtCardholderName, gbc);

        // Card Number
        lblCardNumber = new JLabel("Card Number:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(lblCardNumber, gbc);

        txtCardNumber = new JTextField(20);
        txtCardNumber.setEditable(true);
        gbc.gridx = 1;
        panel.add(txtCardNumber, gbc);

        // Automatically add spaces every 4 digits in the card number field
        txtCardNumber.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String text = txtCardNumber.getText().replaceAll(" ", "");
                if (text.length() > 0) {
                    StringBuilder formattedText = new StringBuilder();
                    for (int i = 0; i < text.length(); i++) {
                        if (i > 0 && i % 4 == 0) {
                            formattedText.append(" ");
                        }
                        formattedText.append(text.charAt(i));
                    }
                    txtCardNumber.setText(formattedText.toString());
                }
            }
        });

        // Expiration Date
        lblExpirationDate = new JLabel("Expiration Date (MM/YY):");
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(lblExpirationDate, gbc);

        txtExpirationDate = new JTextField(7);
        gbc.gridx = 1;
        panel.add(txtExpirationDate, gbc);

        // CVC
        lblCVC = new JLabel("CVC:");
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(lblCVC, gbc);

        txtCVC = new JTextField(4);
        gbc.gridx = 1;
        panel.add(txtCVC, gbc);

        // Check Card Button
        btnCheckCard = new JButton("Check Card");
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(btnCheckCard, gbc);

        add(panel);

        // Check Card Action
        btnCheckCard.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String cardholderName = txtCardholderName.getText().trim();
                String cardNumber = txtCardNumber.getText().replaceAll(" ", "").trim();
                String expirationDate = txtExpirationDate.getText().trim();
                String cvc = txtCVC.getText().trim();

                if (cardholderName.isEmpty() || cardNumber.isEmpty() || expirationDate.isEmpty() || cvc.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Please fill in all fields.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Validate card number length (typically 16 digits)
                if (cardNumber.length() != 16 || !cardNumber.matches("\\d+")) {
                    JOptionPane.showMessageDialog(null, "Invalid card number. Must be 16 digits.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Validate expiration date format (MM/YY)
                if (!expirationDate.matches("(0[1-9]|1[0-2])/[0-9]{2}")) {
                    JOptionPane.showMessageDialog(null, "Invalid expiration date format. Use MM/YY.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Validate CVC (typically 3 digits)
                if (cvc.length() < 3 || cvc.length() > 4 || !cvc.matches("\\d+")) {
                    JOptionPane.showMessageDialog(null, "Invalid CVC. Must be 3 or 4 digits.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }


                checkCardDetails(cardholderName, cardNumber, expirationDate, cvc);
            }
        });
    }

    private void checkCardDetails(String cardholderName, String cardNumber, String expDate, String cvc) {
        String query = "SELECT * FROM cards WHERE CardHoldername = ? AND CardNumber = ? AND ExpirationDate = ? AND cvc = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, cardholderName);
            stmt.setString(2, cardNumber);
            stmt.setString(3, expDate);
            stmt.setString(4, cvc);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                openNewWindow(cardNumber);  // Pass cardNumber as argument
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Card not found or invalid details.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void openNewWindow(String cardNumber) {
        // Assuming MainInterface expects a cardNumber as an argument
        MainInterface balanceWindow = new MainInterface(cardNumber);
        balanceWindow.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ATM atm = new ATM();
            atm.setVisible(true);
        });
    }
}
