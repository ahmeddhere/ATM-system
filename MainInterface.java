import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Optional;
public class MainInterface extends JFrame {
    private String getCardHolderName(String cardNumber) {
        String name = "Unknown";
        String url = "jdbc:mysql://localhost:3306/atm management";
        String dbUser = "root";
        String dbPassword = "";
        String query = "SELECT CardHolderName FROM cards WHERE CardNumber = ?";

        try (Connection conn = DriverManager.getConnection(url, dbUser, dbPassword);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, cardNumber);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                name = rs.getString("CardHolderName").split(" ")[0];
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return name;
    }
    private double balance = 0.0;
    private String currentCardNumber;
    private JLabel lblHeader, lblTimeDate;
    private JPanel mainPanel, buttonPanel;
    private String bankName = "STAR BANK";
    private String timeDate;
    private double pendingTransactionAmount = 0;
    private String pendingTransactionType = "";

    public MainInterface(String cardNumber) {
        currentCardNumber = cardNumber;

        setTitle("ATM Machine");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        timeDate = java.time.LocalDateTime.now().toString();

        JPanel headerPanel = new JPanel(new BorderLayout());
        lblHeader = new JLabel("HI, " + getCardHolderName(currentCardNumber) + "  " + bankName, SwingConstants.CENTER);
        lblHeader.setFont(new Font("Arial", Font.BOLD, 16));
        lblTimeDate = new JLabel(timeDate, SwingConstants.CENTER);
        lblTimeDate.setFont(new Font("Arial", Font.PLAIN, 12));

        headerPanel.add(lblHeader, BorderLayout.NORTH);
        headerPanel.add(lblTimeDate, BorderLayout.SOUTH);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        JLabel lblInstruction = new JLabel("PLEASE SELECT A SERVICE", SwingConstants.CENTER);
        lblInstruction.setFont(new Font("Arial", Font.BOLD, 14));
        mainPanel.add(lblInstruction, BorderLayout.CENTER);

        buttonPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        JButton btnWithdraw = new JButton("WITHDRAW");
        JButton btnDeposit = new JButton("DEPOSIT");
        JButton btnBalance = new JButton("BALANCE ENQUIRY");
        JButton btnChangePin = new JButton("CHANGE PIN");
        JButton btnExit = new JButton("EXIT");

        Font buttonFont = new Font("Arial", Font.BOLD, 14);
        btnWithdraw.setFont(buttonFont);
        btnDeposit.setFont(buttonFont);
        btnBalance.setFont(buttonFont);
        btnChangePin.setFont(buttonFont);
        btnExit.setFont(buttonFont);

        btnWithdraw.addActionListener(e -> showAmountEntryScreen("Withdraw"));
        btnDeposit.addActionListener(e -> showAmountEntryScreen("Deposit"));
        btnBalance.addActionListener(e -> showPinEntryScreen("Balance"));
        btnChangePin.addActionListener(e -> showChangePinScreen());
        btnExit.addActionListener(e -> System.exit(0));

        buttonPanel.add(btnWithdraw);
        buttonPanel.add(btnDeposit);
        buttonPanel.add(btnBalance);
        buttonPanel.add(btnChangePin);
        buttonPanel.add(btnExit);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(mainPanel);

        this.balance = getBalanceFromDatabase(currentCardNumber);
    }

    private void showAmountEntryScreen(String action) {
        pendingTransactionType = action;
        JPanel amountPanel = new JPanel();
        JLabel lblAmount = new JLabel("Enter Amount ($):");
        JTextField txtAmount = new JTextField(10);
        amountPanel.add(lblAmount);
        amountPanel.add(txtAmount);

        int option = JOptionPane.showConfirmDialog(this, amountPanel, action, JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                pendingTransactionAmount = Double.parseDouble(txtAmount.getText());
                if (pendingTransactionType.equals("Withdraw") && pendingTransactionAmount > balance) {
                    JOptionPane.showMessageDialog(this, "Insufficient balance", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                showPinEntryScreen(pendingTransactionType);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid amount", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showPinEntryScreen(String action) {
        JPanel pinEntryPanel = new JPanel();
        JLabel lblEnterPin = new JLabel("Enter PIN:");
        JPasswordField txtPin = new JPasswordField(10);
        pinEntryPanel.add(lblEnterPin);
        pinEntryPanel.add(txtPin);

        int option = JOptionPane.showConfirmDialog(this, pinEntryPanel, "Enter PIN", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String enteredPin = new String(txtPin.getPassword());
            if (isPinCorrect(currentCardNumber, enteredPin)) {
                switch (action) {
                    case "Withdraw":
                        completeWithdrawal(currentCardNumber);
                        break;
                    case "Deposit":
                        completeDeposit(currentCardNumber);
                        break;
                    case "Balance":
                        showBalanceScreen(currentCardNumber);
                        break;
                }
            } else {
                JOptionPane.showMessageDialog(this, "Incorrect PIN", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showChangePinScreen() {
        JPanel panel = new JPanel(new GridLayout(3, 2));
        JPasswordField currentPinField = new JPasswordField();
        JPasswordField newPinField = new JPasswordField();

        panel.add(new JLabel("Current PIN:"));
        panel.add(currentPinField);
        panel.add(new JLabel("New PIN:"));
        panel.add(newPinField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Change PIN", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String currentPin = new String(currentPinField.getPassword());
            String newPin = new String(newPinField.getPassword());
            if (!newPin.matches("\\d+")) {
                JOptionPane.showMessageDialog(this, "PIN must contain only numbers", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!isPinCorrect(currentCardNumber, currentPin)) {
                JOptionPane.showMessageDialog(this, "Incorrect current PIN", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (newPin.length() != 4) {
                JOptionPane.showMessageDialog(this, "New PIN must be exactly 4 digits", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            updatePinInDatabase(currentCardNumber, newPin);
            JOptionPane.showMessageDialog(this, "PIN changed successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void updatePinInDatabase(String cardNumber, String newPin) {
        String url = "jdbc:mysql://localhost:3306/atm management";
        String dbUser = "root";
        String dbPassword = "";
        String query = "UPDATE cards SET pin = ? WHERE CardNumber = ?";

        try (Connection conn = DriverManager.getConnection(url, dbUser, dbPassword);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, newPin);
            stmt.setString(2, cardNumber);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private boolean isPinCorrect(String cardNumber, String enteredPin) {
        boolean isValid = false;
        String url = "jdbc:mysql://localhost:3306/atm management";
        String dbUser = "root";
        String dbPassword = "";
        String query = "SELECT pin FROM cards WHERE CardNumber = ?";

        try (Connection conn = DriverManager.getConnection(url, dbUser, dbPassword);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, cardNumber);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String storedPin = rs.getString("pin");
                if (storedPin.trim().equals(enteredPin.trim())) {
                    isValid = true;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return isValid;
    }

    private void completeWithdrawal(String cardNumber) {
        balance -= pendingTransactionAmount;
        updateBalanceInDatabase(cardNumber, balance);
        JOptionPane.showMessageDialog(this, "Withdrawal successful!\nAmount: $" + pendingTransactionAmount + "\nNew balance: $" + balance);
        pendingTransactionAmount = 0;
    }

    private void completeDeposit(String cardNumber) {
        balance += pendingTransactionAmount;
        updateBalanceInDatabase(cardNumber, balance);
        JOptionPane.showMessageDialog(this, "Deposit successful!\nAmount: $" + pendingTransactionAmount + "\nNew balance: $" + balance);
        pendingTransactionAmount = 0;
    }

    private void showBalanceScreen(String cardNumber) {
        double balance = getBalanceFromDatabase(cardNumber);
        JOptionPane.showMessageDialog(this, "Your current balance is: $" + balance);
    }

    private double getBalanceFromDatabase(String cardNumber) {
        double balance = 0.0;
        String url = "jdbc:mysql://localhost:3306/atm management";
        String dbUser = "root";
        String dbPassword = "";
        String query = "SELECT balance FROM cards WHERE CardNumber = ?";

        try (Connection conn = DriverManager.getConnection(url, dbUser, dbPassword);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, cardNumber);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                balance = rs.getDouble("balance");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return balance;
    }

    private void updateBalanceInDatabase(String cardNumber, double newBalance) {
        String url = "jdbc:mysql://localhost:3306/atm management";
        String dbUser = "root";
        String dbPassword = "";
        String query = "UPDATE cards SET balance = ? WHERE CardNumber = ?";

        try (Connection conn = DriverManager.getConnection(url, dbUser, dbPassword);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setDouble(1, newBalance);
            stmt.setString(2, cardNumber);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String cardNumber = "123456789";
        SwingUtilities.invokeLater(() -> {
            MainInterface atm = new MainInterface(cardNumber);
            atm.setVisible(true);
        });
    }
}
