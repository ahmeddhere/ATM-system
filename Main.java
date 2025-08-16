import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.Calendar;

public class Main extends JFrame {

    private JLabel lblTitle, lblName, lblCardNumber, lblExpirationDate, lblCVC, lblPin,
            lblCardDetailsTitle, lblCardHolderDisplay, lblCardNumberDisplay,
            lblExpirationDateDisplay, lblCVCDisplay, lblPinDisplay, lblRegistrationSuccess;
    private JTextField txtName, txtCardNumber, txtExpirationDate, txtCVC, txtPin;
    private JButton btnRegister, btnNext;
    private JPanel panel;

    private Set<String> generatedCardNumbers = new HashSet<>();
    private Set<String> generatedCVCs = new HashSet<>();

    private static final String URL = "jdbc:mysql://localhost:3306/atm management"; // Updated database name
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public Main() {
        setTitle("Star Bank ATM Card Registration");
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 600);
        setLocationRelativeTo(null);

        panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Form Title
        lblTitle = new JLabel("Star Bank ATM Card Registration Form");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitle.setForeground(Color.BLUE);
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(lblTitle, gbc);

        // Name Field
        lblName = new JLabel("Card Holder's Name:");
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(lblName, gbc);

        txtName = new JTextField(20);
        gbc.gridx = 1;
        panel.add(txtName, gbc);

        // Prevent numbers in the cardholder name field
        txtName.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                // Allow spaces but prevent numbers
                if (Character.isDigit(c)) {
                    e.consume(); // Ignore the key if it's a number
                    JOptionPane.showMessageDialog(null, "Please enter only letters and spaces for the cardholder's name.",
                            "Invalid Input", JOptionPane.ERROR_MESSAGE);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                // Convert the text to uppercase when the user types
                String text = txtName.getText();
                txtName.setText(text.toUpperCase());
            }
        });

        // Card Number Field (Disabled)
        lblCardNumber = new JLabel("Card Number:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(lblCardNumber, gbc);

        txtCardNumber = new JTextField(20);
        txtCardNumber.setEditable(false); // Disable editing
        gbc.gridx = 1;
        panel.add(txtCardNumber, gbc);

        // Expiration Date Field
        lblExpirationDate = new JLabel("Expiration Date (MM/YY):");
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(lblExpirationDate, gbc);

        txtExpirationDate = new JTextField(7);
        txtExpirationDate.setEditable(false);
        gbc.gridx = 1;
        panel.add(txtExpirationDate, gbc);

        // CVC Field
        lblCVC = new JLabel("CVC:");
        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(lblCVC, gbc);

        txtCVC = new JTextField(4);
        txtCVC.setEditable(false);
        gbc.gridx = 1;
        panel.add(txtCVC, gbc);

        // PIN Field (New addition)
        lblPin = new JLabel("PIN (4 digits):");
        gbc.gridx = 0;
        gbc.gridy = 5;
        panel.add(lblPin, gbc);

        txtPin = new JTextField(4);
        gbc.gridx = 1;
        panel.add(txtPin, gbc);

        // Validate PIN (only numbers and 4 digits)
        txtPin.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c) || txtPin.getText().length() >= 4) {
                    e.consume(); // Ignore the key if it's not a digit or exceeds 4 digits
                }
            }
        });

        // Register Button
        btnRegister = new JButton("Register");
        btnRegister.setBackground(new Color(60, 179, 113));
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 6;
        panel.add(btnRegister, gbc);

        // Card Details Section
        lblCardDetailsTitle = new JLabel("Card Details:");
        lblCardDetailsTitle.setFont(new Font("Arial", Font.BOLD, 14));
        lblCardDetailsTitle.setForeground(Color.BLUE);
        gbc.gridx = 0;
        gbc.gridy = 7;
        panel.add(lblCardDetailsTitle, gbc);

        lblCardHolderDisplay = new JLabel("");
        gbc.gridx = 0;
        gbc.gridy = 8;
        panel.add(lblCardHolderDisplay, gbc);

        lblCardNumberDisplay = new JLabel("");
        gbc.gridx = 0;
        gbc.gridy = 9;
        panel.add(lblCardNumberDisplay, gbc);

        lblExpirationDateDisplay = new JLabel("");
        gbc.gridx = 0;
        gbc.gridy = 10;
        panel.add(lblExpirationDateDisplay, gbc);

        lblCVCDisplay = new JLabel("");
        gbc.gridx = 0;
        gbc.gridy = 11;
        panel.add(lblCVCDisplay, gbc);

        lblPinDisplay = new JLabel("");
        gbc.gridx = 0;
        gbc.gridy = 12;
        panel.add(lblPinDisplay, gbc);

        // Success Message
        lblRegistrationSuccess = new JLabel("");
        lblRegistrationSuccess.setFont(new Font("Arial", Font.BOLD, 14));
        lblRegistrationSuccess.setForeground(Color.GREEN);
        gbc.gridx = 0;
        gbc.gridy = 13;
        panel.add(lblRegistrationSuccess, gbc);

        // Next Button at the bottom right corner
        btnNext = new JButton("Next");
        btnNext.setBackground(new Color(60, 179, 113));
        btnNext.setForeground(Color.WHITE);
        btnNext.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridwidth = 1;
        gbc.gridx = 1;
        gbc.gridy = 14;
        panel.add(btnNext, gbc);

        add(panel, BorderLayout.CENTER);

        // Register Button Action
        btnRegister.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = txtName.getText().trim();
                String pin = txtPin.getText().trim();

                if (name.isEmpty() || pin.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Please enter both the cardholder's name and PIN.",
                            "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (pin.length() != 4) {
                    JOptionPane.showMessageDialog(null, "PIN must be exactly 4 digits.",
                            "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String cardNumber = generateUniqueCardNumber();
                String formattedCardNumber = formatCardNumberWithSpaces(cardNumber); // Format for display with spaces every 4 digits
                String expirationDate = generateExpirationDate();
                String cvc = generateUniqueCVC();

                // Display all details
                lblCardHolderDisplay.setText("Cardholder: " + name);
                lblCardNumberDisplay.setText("Card Number: " + formattedCardNumber);
                lblExpirationDateDisplay.setText("Expiration Date: " + expirationDate);
                lblCVCDisplay.setText("CVC: " + cvc);
                lblPinDisplay.setText("PIN: " + pin);
                lblRegistrationSuccess.setText("Registration Successful!");

                // Insert into database with no spaces
                insertCardDetails(cardNumber, name, expirationDate, cvc, pin);

                // Disable fields after registration
                txtName.setEditable(false);
                txtPin.setEditable(false);
                btnRegister.setEnabled(false);
            }
        });

        // Action for "Next" button
        btnNext.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Create and display the new window (ATM)
                ATM atmWindow = new ATM();
                atmWindow.setVisible(true);

                // Close the current window (Main)
                dispose();
            }
        });
    }

    // Generate a unique card number
    private String generateUniqueCardNumber() {
        Random random = new Random();
        StringBuilder cardNumber = new StringBuilder();

        while (true) {
            cardNumber.setLength(0); // Reset the card number each time

            // Generate a 16-digit card number
            for (int i = 0; i < 16; i++) {
                cardNumber.append(random.nextInt(10)); // Append random digit
                if ((i + 1) % 4 == 0 && i < 15) {
                    cardNumber.append(" "); // Add a space every 4 digits
                }
            }

            // Check if the card number is unique
            if (!generatedCardNumbers.contains(cardNumber.toString())) {
                generatedCardNumbers.add(cardNumber.toString()); // Add the number to the set
                break; // Exit loop if unique
            }
        }

        return cardNumber.toString(); // Return the unique card number
    }

    // Format the card number with spaces
    private String formatCardNumberWithSpaces(String cardNumber) {
        return cardNumber.replaceAll("(.{4})(?=.)", "$1 "); // Format the card number with spaces every 4 digits
    }

    // Generate the expiration date (5 years from today)
    private String generateExpirationDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 5); // Set expiration to 5 years from now
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR) % 100; // Get the last 2 digits of the year
        return String.format("%02d/%02d", month, year);
    }

    // Generate a unique CVC
    private String generateUniqueCVC() {
        Random random = new Random();
        int cvc = random.nextInt(900) + 100; // Generate a random 3-digit CVC
        return String.valueOf(cvc);
    }

    // Insert card details into the database
    private void insertCardDetails(String cardNumber, String name, String expDate, String cvc, String pin) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO cards (CardNumber, CardHoldername, ExpirationDate, cvc, Pin) VALUES (?, ?, ?, ?, ?)")) {

            stmt.setString(1, cardNumber.replace(" ", "")); // Store without spaces
            stmt.setString(2, name);
            stmt.setString(3, expDate);
            stmt.setString(4, cvc);
            stmt.setString(5, pin);  // Insert PIN into database
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
}
