import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class AppGUI extends JFrame {
    private LostAndFoundSystem system;
    private User currentUser;
    
    private JTable lostTable, foundTable;
    private DefaultTableModel lostModel, foundModel;
    private JTextArea logArea;
    private JTabbedPane tabbedPane;

    public AppGUI() {
        system = new LostAndFoundSystem();
        showLoginDialog();
        
        if (currentUser == null) {
            System.exit(0);
        }

        String roleTitle = currentUser.isAdmin() ? " [ADMINISTRATOR MODE]" : " [STUDENT MODE]";
        setTitle("Lost & Found DBMS Enterprise System - Logged in as: " + currentUser.getName() + roleTitle);
        setSize(980, 680);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Dashboard & Items", createDashboardPanel());
        tabbedPane.addTab("Report Lost Item", createReportLostPanel());
        tabbedPane.addTab("Report Found Item", createReportFoundPanel());
        tabbedPane.addTab("Claim Item ", createClaimPanel());
        
        if (currentUser.isAdmin()) {
            tabbedPane.addTab("Admin DBMS Utilities (EOD & Views)", createAdminUtilitiesPanel());
        }

        add(tabbedPane, BorderLayout.CENTER);

        logArea = new JTextArea(6, 80);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logArea.setBackground(new Color(245, 245, 245));
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createTitledBorder("DBMS Output & Action Log"));
        add(logScroll, BorderLayout.SOUTH);

        refreshTables();
    }

    private void showLoginDialog() {
        JDialog authDialog = new JDialog((Frame) null, "System Authentication", true);
        authDialog.setSize(420, 320);
        authDialog.setLocationRelativeTo(null);

        JTabbedPane authTabs = new JTabbedPane();

        // TAB 1: USER LOGIN
        JPanel loginPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        loginPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JTextField loginIdField = new JTextField();
        JTextField loginNameField = new JTextField();
        loginPanel.add(new JLabel("User ID (Number):")); loginPanel.add(loginIdField);
        loginPanel.add(new JLabel("Full Name:")); loginPanel.add(loginNameField);
        JButton loginBtn = new JButton("Login");
        loginBtn.addActionListener(e -> {
            try {
                int uid = Integer.parseInt(loginIdField.getText().trim());
                String uname = loginNameField.getText().trim();
                User user = system.loginUser(uid, uname);
                if (user != null) {
                    currentUser = user;
                    authDialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(authDialog, "Invalid User ID or Name combination.", "Login Failed", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(authDialog, "User ID must be a numeric integer.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        loginPanel.add(new JLabel()); loginPanel.add(loginBtn);

        // TAB 2: REGISTER NEW USER
        JPanel regPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        regPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        JTextField regIdField = new JTextField();
        JTextField regNameField = new JTextField();
        JTextField regEmailField = new JTextField();
        JTextField regPhoneField = new JTextField();
        regPanel.add(new JLabel("User ID (Number):")); regPanel.add(regIdField);
        regPanel.add(new JLabel("Full Name:")); regPanel.add(regNameField);
        regPanel.add(new JLabel("Email:")); regPanel.add(regEmailField);
        regPanel.add(new JLabel("Phone:")); regPanel.add(regPhoneField);
        JButton regBtn = new JButton("Register & Login");
        regBtn.addActionListener(e -> {
            try {
                int uid = Integer.parseInt(regIdField.getText().trim());
                String name = regNameField.getText().trim();
                String email = regEmailField.getText().trim();
                String phone = regPhoneField.getText().trim();
                if (name.isEmpty()) {
                    JOptionPane.showMessageDialog(authDialog, "Name cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                User newUser = new User(uid, name, email, phone);
                if (system.registerUser(newUser)) {
                    currentUser = newUser;
                    authDialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(authDialog, "Registration failed. User ID might already exist.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(authDialog, "User ID must be numeric.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        regPanel.add(new JLabel()); regPanel.add(regBtn);

        // TAB 3: ADMIN LOGIN
        JPanel adminPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        adminPanel.setBorder(BorderFactory.createEmptyBorder(30, 20, 30, 20));
        JPasswordField adminPassField = new JPasswordField();
        adminPanel.add(new JLabel("Admin Password:")); adminPanel.add(adminPassField);
        JButton adminBtn = new JButton("Admin Login");
        adminBtn.addActionListener(e -> {
            String pass = new String(adminPassField.getPassword());
            if (pass.equals("4207")) {
                currentUser = new User(9999, "System Admin", true);
                authDialog.dispose();
            } else {
                JOptionPane.showMessageDialog(authDialog, "Incorrect Admin Password!", "Access Denied", JOptionPane.ERROR_MESSAGE);
            }
        });
        adminPanel.add(new JLabel()); adminPanel.add(adminBtn);

        authTabs.addTab("Login", loginPanel);
        authTabs.addTab("Register New User", regPanel);
        authTabs.addTab("Admin Login", adminPanel);

        authDialog.add(authTabs);
        authDialog.setVisible(true);
    }

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 10, 10));

        lostModel = new DefaultTableModel(new String[]{"ID", "Item Name", "Category", "Description", "Location", "Date Reported", "Contact"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        lostTable = new JTable(lostModel);
        lostTable.getTableHeader().setReorderingAllowed(false);
        JScrollPane lostSp = new JScrollPane(lostTable);
        lostSp.setBorder(BorderFactory.createTitledBorder("Lost Items Listing"));

        foundModel = new DefaultTableModel(new String[]{"ID", "Item Name", "Category", "Description", "Location", "Date Reported", "Contact"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        foundTable = new JTable(foundModel);
        foundTable.getTableHeader().setReorderingAllowed(false);
        JScrollPane foundSp = new JScrollPane(foundTable);
        foundSp.setBorder(BorderFactory.createTitledBorder("Found Items Listing"));

        panel.add(lostSp);
        panel.add(foundSp);
        return panel;
    }

    private JPanel createDatePanel(JComboBox<String> dayBox, JComboBox<String> monthBox, JComboBox<String> yearBox) {
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        for (int i = 1; i <= 31; i++) dayBox.addItem(String.format("%02d", i));
        for (int i = 1; i <= 12; i++) monthBox.addItem(String.format("%02d", i));
        yearBox.addItem("2026"); yearBox.addItem("2025"); yearBox.addItem("2024");
        
        datePanel.add(new JLabel("Day:")); datePanel.add(dayBox);
        datePanel.add(new JLabel("Month:")); datePanel.add(monthBox);
        datePanel.add(new JLabel("Year:")); datePanel.add(yearBox);
        return datePanel;
    }

    private JPanel createReportLostPanel() {
        JPanel panel = new JPanel(new GridLayout(7, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        JTextField nameField = new JTextField();
        JComboBox<String> catBox = new JComboBox<>(LostAndFoundSystem.STANDARDIZED_CATEGORIES);
        JTextField descField = new JTextField();
        JTextField locField = new JTextField();
        
        JComboBox<String> dayBox = new JComboBox<>();
        JComboBox<String> monthBox = new JComboBox<>();
        JComboBox<String> yearBox = new JComboBox<>();
        JPanel datePanel = createDatePanel(dayBox, monthBox, yearBox);
        
        JTextField contactField = new JTextField();

        panel.add(new JLabel("Item Name:")); panel.add(nameField);
        panel.add(new JLabel("Standardized Category (3NF):")); panel.add(catBox);
        panel.add(new JLabel("Description:")); panel.add(descField);
        panel.add(new JLabel("Location Lost:")); panel.add(locField);
        panel.add(new JLabel("Date Lost:")); panel.add(datePanel);
        panel.add(new JLabel("Contact Info:")); panel.add(contactField);

        JButton submitBtn = new JButton("Report Lost Item");
        submitBtn.addActionListener(e -> {
            int d = Integer.parseInt((String) dayBox.getSelectedItem());
            int m = Integer.parseInt((String) monthBox.getSelectedItem());
            int y = Integer.parseInt((String) yearBox.getSelectedItem());

            if (!LostAndFoundSystem.isValidDate(d, m, y)) {
                JOptionPane.showMessageDialog(this, "Selected date is invalid or in the future!", "Date Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String formattedDate = String.format("%02d-%02d-%d", d, m, y);
            Lost item = new Lost(nameField.getText().trim(), (String) catBox.getSelectedItem(), descField.getText().trim(), locField.getText().trim(), formattedDate, contactField.getText().trim(), currentUser.getUserId());
            
            String result = system.addLostItem(item);
            logArea.setText(result);
            refreshTables();
            JOptionPane.showMessageDialog(this, "Lost item reported successfully!");
            nameField.setText(""); descField.setText(""); locField.setText(""); contactField.setText("");
            tabbedPane.setSelectedIndex(0);
        });

        panel.add(new JLabel()); panel.add(submitBtn);
        return panel;
    }

    private JPanel createReportFoundPanel() {
        JPanel panel = new JPanel(new GridLayout(7, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        JTextField nameField = new JTextField();
        JComboBox<String> catBox = new JComboBox<>(LostAndFoundSystem.STANDARDIZED_CATEGORIES);
        JTextField descField = new JTextField();
        JTextField locField = new JTextField();
        
        JComboBox<String> dayBox = new JComboBox<>();
        JComboBox<String> monthBox = new JComboBox<>();
        JComboBox<String> yearBox = new JComboBox<>();
        JPanel datePanel = createDatePanel(dayBox, monthBox, yearBox);

        JTextField contactField = new JTextField();

        panel.add(new JLabel("Item Name:")); panel.add(nameField);
        panel.add(new JLabel("Standardized Category (3NF):")); panel.add(catBox);
        panel.add(new JLabel("Description:")); panel.add(descField);
        panel.add(new JLabel("Location Found:")); panel.add(locField);
        panel.add(new JLabel("Date Found:")); panel.add(datePanel);
        panel.add(new JLabel("Contact Info:")); panel.add(contactField);

        JButton submitBtn = new JButton("Report Found Item");
        submitBtn.addActionListener(e -> {
            int d = Integer.parseInt((String) dayBox.getSelectedItem());
            int m = Integer.parseInt((String) monthBox.getSelectedItem());
            int y = Integer.parseInt((String) yearBox.getSelectedItem());

            if (!LostAndFoundSystem.isValidDate(d, m, y)) {
                JOptionPane.showMessageDialog(this, "Selected date is invalid or in the future!", "Date Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String formattedDate = String.format("%02d-%02d-%d", d, m, y);
            Found item = new Found(nameField.getText().trim(), (String) catBox.getSelectedItem(), descField.getText().trim(), locField.getText().trim(), formattedDate, contactField.getText().trim(), currentUser.getUserId());
            
            String result = system.addFoundItem(item);
            logArea.setText(result);
            refreshTables();
            JOptionPane.showMessageDialog(this, "Found item reported successfully!");
            nameField.setText(""); descField.setText(""); locField.setText(""); contactField.setText("");
            tabbedPane.setSelectedIndex(0);
        });

        panel.add(new JLabel()); panel.add(submitBtn);
        return panel;
    }

    private JPanel createClaimPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        JTextField foundIdField = new JTextField();
        JTextField lostIdField = new JTextField();

        panel.add(new JLabel("Found Item ID to Claim (Required):")); panel.add(foundIdField);
        panel.add(new JLabel("Your Matching Lost Item ID (Required for Students):")); panel.add(lostIdField);

        JButton claimBtn = new JButton("Execute Authorized ACID Claim");
        claimBtn.addActionListener(e -> {
            try {
                int fid = Integer.parseInt(foundIdField.getText().trim());
                int lid = lostIdField.getText().trim().isEmpty() ? 0 : Integer.parseInt(lostIdField.getText().trim());
                
                // Passes User ID and Admin flag for verification
                String result = system.claimItemWithACID(fid, lid, currentUser.getUserId(), currentUser.isAdmin());
                logArea.setText(result);
                refreshTables();
                if (result.contains("committed atomically")) {
                    JOptionPane.showMessageDialog(this, "Item claimed successfully!");
                    tabbedPane.setSelectedIndex(0);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Item IDs must be valid numeric integers.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(new JLabel()); panel.add(claimBtn);
        return panel;
    }

    private JPanel createAdminUtilitiesPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 50));

        JButton eodBtn = new JButton("Run End-Of-Day (EOD) Batch Stored Procedure");
        eodBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        eodBtn.addActionListener(e -> {
            String res = system.runEODBatchProcess();
            logArea.setText(res);
            refreshTables();
        });

        JButton refreshBtn = new JButton("Force Refresh SQL Views & Tables");
        refreshBtn.addActionListener(e -> {
            refreshTables();
            logArea.setText("[ADMIN ACTION] Database views re-queried and GUI models updated.");
        });

        panel.add(eodBtn);
        panel.add(refreshBtn);
        return panel;
    }

    private void refreshTables() {
        lostModel.setRowCount(0);
        for (Lost l : system.getLostItemsFromView()) {
            lostModel.addRow(new Object[]{l.getId(), l.getItemName(), l.getCategory(), l.getDescription(), l.getLocation(), l.getDate(), l.getContact()});
        }

        foundModel.setRowCount(0);
        for (Found f : system.getFoundItemsFromView()) {
            foundModel.addRow(new Object[]{f.getId(), f.getItemName(), f.getCategory(), f.getDescription(), f.getLocation(), f.getDate(), f.getContact()});
        }
    }
}