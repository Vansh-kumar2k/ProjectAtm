import org.w3c.dom.ls.LSOutput;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ATM.java - Improved Swing UI for ATM Simulation
 * Drop-in replacement for a basic ATM console; uses in-memory state.
 */
public class ATM extends JFrame {
    // UI components
    private String shutup;
    private String rrrrrrrrrrrrrrr;
    private String ppopop;
    private CardLayout cards;
    private JPanel cardRoot;
/// testing this
    // Login components
    private JPasswordField pinField;
    private final String CORRECT_PIN = "1234"; // change as needed for demo

    // Dashboard components
    private JLabel lblBalance;
    private DefaultTableModel txnModel;
    private JTable txnTable;
    private NumberFormat currencyFmt = NumberFormat.getCurrencyInstance();

    // Account state
    private double balance = 5000.00; // sample starting balance

    public ATM() {
        super("ATM Simulator");
        initLookAndFeel();
        initUI();
    }

    private void initLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    UIManager.put("Button.focus", Color.GRAY);
                    break;
                }
            }
        } catch (Exception ignored) {}
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(760, 520);
        setLocationRelativeTo(null);

        cards = new CardLayout();
        cardRoot = new JPanel(cards);
        cardRoot.setBorder(new EmptyBorder(12, 12, 12, 12));
        add(cardRoot);

        cardRoot.add(loginPanel(), "login");
        cardRoot.add(dashboardPanel(), "dashboard");

        cards.show(cardRoot, "login");
        setVisible(true);
    }

    // ---------------- LOGIN PANEL ----------------
    private JPanel loginPanel() {
        JPanel p = new JPanel(new BorderLayout(10,10));
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220,220,220)),
                new EmptyBorder(18,18,18,18)
        ));

        JLabel title = new JLabel("Welcome to Simple ATM", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        p.add(title, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8,8,8,8);
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        JLabel info = new JLabel("Enter your 4-digit PIN to continue:");
        center.add(info, c);

        c.gridy = 1; c.gridwidth = 1;
        center.add(new JLabel("PIN:"), c);

        pinField = new JPasswordField(6);
        pinField.setEchoChar('•');
        pinField.setFont(new Font("Monospaced", Font.PLAIN, 16));
        c.gridx = 1;
        center.add(pinField, c);

        // small numeric keypad
        JPanel keypad = numericKeypad(pinField);
        c.gridx = 0; c.gridy = 2; c.gridwidth = 2;
        center.add(keypad, c);

        p.add(center, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnLogin = new JButton("Login");
        JButton btnExit = new JButton("Exit");
        btnLogin.setPreferredSize(new Dimension(100, 36));
        btnExit.setPreferredSize(new Dimension(100, 36));
        bottom.add(btnExit);
        bottom.add(btnLogin);
        p.add(bottom, BorderLayout.SOUTH);

        btnLogin.addActionListener(e -> attemptLogin());
        pinField.addActionListener(e -> attemptLogin());
        btnExit.addActionListener(e -> System.exit(0));

        return p;
    }

    private void attemptLogin() {
        String pin = new String(pinField.getPassword()).trim();
        if (pin.equals(CORRECT_PIN)) {
            pinField.setText("");
            refreshBalanceLabel();
            cards.show(cardRoot, "dashboard");
        } else {
            JOptionPane.showMessageDialog(this, "Incorrect PIN. Try again.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            pinField.setText("");
        }
    }

    private JPanel numericKeypad(JTextField target) {
        JPanel kp = new JPanel(new GridLayout(4,3,6,6));
        kp.setBorder(new EmptyBorder(8,40,8,40));
        for (int i=1;i<=9;i++) {
            String s = String.valueOf(i);
            JButton b = new JButton(s);
            b.addActionListener(e -> target.setText(target.getText() + s));
            kp.add(b);
        }
        JButton bClear = new JButton("C");
        bClear.addActionListener(e -> target.setText(""));
        JButton b0 = new JButton("0");
        b0.addActionListener(e -> target.setText(target.getText() + "0"));
        JButton bBack = new JButton("⌫");
        bBack.addActionListener(e -> {
            String t = target.getText();
            if (!t.isEmpty()) target.setText(t.substring(0, t.length()-1));
        });
        kp.add(bClear);
        kp.add(b0);
        kp.add(bBack);
        return kp;
    }

    // ---------------- DASHBOARD PANEL ----------------
    private JPanel dashboardPanel() {
        JPanel root = new JPanel(new BorderLayout(12,12));

        // Top: header + actions
        JPanel header = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Account Dashboard");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        header.add(title, BorderLayout.WEST);

        JPanel topRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8,0));
        JButton btnLogout = new JButton("Logout");
        JButton btnExit = new JButton("Exit");
        btnLogout.setPreferredSize(new Dimension(90, 30));
        btnExit.setPreferredSize(new Dimension(90, 30));
        topRight.add(btnLogout);
        topRight.add(btnExit);
        header.add(topRight, BorderLayout.EAST);
        root.add(header, BorderLayout.NORTH);

        // Center: Left (controls) and right (transactions)
        JPanel center = new JPanel(new BorderLayout(10,10));

        // Left card with balance + operations
        JPanel left = new JPanel(new BorderLayout(8,8));
        left.setPreferredSize(new Dimension(360,300));
        left.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230,230,230)),
                new EmptyBorder(12,12,12,12)
        ));

        // Balance block
        JPanel balPanel = new JPanel(new GridBagLayout());
        GridBagConstraints bc = new GridBagConstraints();
        bc.insets = new Insets(6,6,6,6);
        bc.gridx=0; bc.gridy=0; bc.anchor = GridBagConstraints.WEST;
        JLabel lblBalTitle = new JLabel("Current Balance");
        lblBalTitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        bc.gridy++;
        lblBalance = new JLabel(currencyFmt.format(balance));
        lblBalance.setFont(new Font("SansSerif", Font.BOLD, 18));
        balPanel.add(lblBalTitle, bc);
        bc.gridy++;
        balPanel.add(lblBalance, bc);

        left.add(balPanel, BorderLayout.NORTH);

        // Operations tabs (Deposit / Withdraw)
        JTabbedPane ops = new JTabbedPane();
        ops.addTab("Deposit", depositPanel());
        ops.addTab("Withdraw", withdrawPanel());
        left.add(ops, BorderLayout.CENTER);

        center.add(left, BorderLayout.WEST);

        // Right: transaction history
        JPanel right = new JPanel(new BorderLayout());
        right.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230,230,230)),
                new EmptyBorder(12,12,12,12)
        ));
        JLabel txnTitle = new JLabel("Transaction History");
        txnTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        right.add(txnTitle, BorderLayout.NORTH);

        txnModel = new DefaultTableModel(new String[]{"Time","Type","Amount","Balance"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        txnTable = new JTable(txnModel);
        txnTable.setRowHeight(26);
        txnTable.setFillsViewportHeight(true);
        txnTable.getColumnModel().getColumn(0).setPreferredWidth(160);
        JTableHeader th = txnTable.getTableHeader();
        th.setFont(new Font("SansSerif", Font.BOLD, 12));
        JScrollPane sp = new JScrollPane(txnTable);
        sp.setPreferredSize(new Dimension(360,320));
        right.add(sp, BorderLayout.CENTER);

        center.add(right, BorderLayout.CENTER);

        root.add(center, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel tip = new JLabel("Tip: Use the tabs to Deposit/Withdraw. Logout to return to PIN screen.");
        tip.setFont(new Font("SansSerif", Font.PLAIN, 12));
        footer.add(tip);
        root.add(footer, BorderLayout.SOUTH);

        // Button actions
        btnLogout.addActionListener(e -> cards.show(cardRoot, "login"));
        btnExit.addActionListener(e -> System.exit(0));

        return root;
    }

    // Deposit panel
    private JPanel depositPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8,8,8,8);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx=0; c.gridy=0;
        p.add(new JLabel("Amount to deposit"), c);

        JTextField tfAmount = new JTextField();
        tfAmount.setFont(new Font("SansSerif", Font.PLAIN, 14));
        c.gridx=1; c.weightx = 1.0;
        p.add(tfAmount, c);

        JButton btnQuick100 = new JButton("+100");
        JButton btnQuick500 = new JButton("+500");
        JButton btnQuick1000 = new JButton("+1000");
        JPanel quick = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        quick.add(btnQuick100); quick.add(btnQuick500); quick.add(btnQuick1000);
        c.gridx=0; c.gridy=1; c.gridwidth = 2;
        p.add(quick, c);
        c.gridwidth=1;

        JButton btnDeposit = new JButton("Deposit");
        btnDeposit.setPreferredSize(new Dimension(120,36));
        c.gridx=0; c.gridy=2; c.gridwidth=2; c.anchor = GridBagConstraints.CENTER;
        p.add(btnDeposit, c);

        // actions
        btnQuick100.addActionListener(e -> tfAmount.setText(incr(tfAmount.getText(), "100")));
        btnQuick500.addActionListener(e -> tfAmount.setText(incr(tfAmount.getText(), "500")));
        btnQuick1000.addActionListener(e -> tfAmount.setText(incr(tfAmount.getText(), "1000")));

        btnDeposit.addActionListener(e -> {
            String text = tfAmount.getText().trim();
            double amt = parsePositive(text);
            if (amt <= 0) {
                JOptionPane.showMessageDialog(this, "Enter a valid positive amount.", "Invalid Amount", JOptionPane.WARNING_MESSAGE);
                return;
            }
            balance += amt;
            addTransaction("Deposit", amt);
            refreshBalanceLabel();
            tfAmount.setText("");
            JOptionPane.showMessageDialog(this, "Deposit successful.", "Success", JOptionPane.INFORMATION_MESSAGE);
        });

        return p;
    }

    // Withdraw panel
    private JPanel withdrawPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8,8,8,8);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx=0; c.gridy=0;
        p.add(new JLabel("Amount to withdraw"), c);

        JTextField tfAmount = new JTextField();
        tfAmount.setFont(new Font("SansSerif", Font.PLAIN, 14));
        c.gridx=1; c.weightx = 1.0;
        p.add(tfAmount, c);

        JButton btnQuick100 = new JButton("-100");
        JButton btnQuick500 = new JButton("-500");
        JButton btnQuick1000 = new JButton("-1000");
        JPanel quick = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        quick.add(btnQuick100); quick.add(btnQuick500); quick.add(btnQuick1000);
        c.gridx=0; c.gridy=1; c.gridwidth = 2;
        p.add(quick, c);
        c.gridwidth=1;

        JButton btnWithdraw = new JButton("Withdraw");
        btnWithdraw.setPreferredSize(new Dimension(120,36));
        c.gridx=0; c.gridy=2; c.gridwidth=2; c.anchor = GridBagConstraints.CENTER;
        p.add(btnWithdraw, c);

        // actions
        btnQuick100.addActionListener(e -> tfAmount.setText(incr(tfAmount.getText(), "100")));
        btnQuick500.addActionListener(e -> tfAmount.setText(incr(tfAmount.getText(), "500")));
        btnQuick1000.addActionListener(e -> tfAmount.setText(incr(tfAmount.getText(), "1000")));

        btnWithdraw.addActionListener(e -> {
            String text = tfAmount.getText().trim();
            double amt = parsePositive(text);
            if (amt <= 0) {
                JOptionPane.showMessageDialog(this, "Enter a valid positive amount.", "Invalid Amount", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (amt > balance) {
                JOptionPane.showMessageDialog(this, "Insufficient balance.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            balance -= amt;
            addTransaction("Withdraw", amt);
            refreshBalanceLabel();
            tfAmount.setText("");
            JOptionPane.showMessageDialog(this, "Withdrawal successful.", "Success", JOptionPane.INFORMATION_MESSAGE);
        });

        return p;
    }

    private String incr(String current, String add) {
        try {
            double cur = current.isEmpty() ? 0.0 : Double.parseDouble(current);
            cur += Double.parseDouble(add);
            if (cur == (long) cur) return String.valueOf((long)cur);
            return String.valueOf(cur);
        } catch (NumberFormatException ex) {
            return add;
        }
    }

    private double parsePositive(String s) {
        try {
            double v = Double.parseDouble(s);
            if (v <= 0) return -1;
            return v;
        } catch (Exception e) {
            return -1;
        }
    }

    private void addTransaction(String type, double amount) {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        txnModel.insertRow(0, new Object[] { time, type, currencyFmt.format(amount), currencyFmt.format(balance) });
    }

    private void refreshBalanceLabel() {
        lblBalance.setText(currencyFmt.format(balance));
    }

    // ----------------- main -----------------
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ATM());
    }
}
