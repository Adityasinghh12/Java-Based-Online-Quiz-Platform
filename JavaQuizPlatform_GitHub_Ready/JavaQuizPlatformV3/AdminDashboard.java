import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * AdminDashboard.java
 * Screen shown after an Admin logs in. Five tabs, matching the Admin's
 * responsibilities: manage users, approve/reject quiz content, manage
 * system settings, view performance reports, and see system alerts.
 */
public class AdminDashboard extends JFrame {
    private DefaultTableModel usersModel;
    private JTable usersTable;

    private DefaultTableModel quizzesModel;
    private JTable quizzesTable;

    private DefaultTableModel alertsModel;

    private PerformanceChartPanel chartPanel;

    public AdminDashboard() {
        setTitle("Admin Dashboard - Java Quiz Platform");
        setSize(880, 580);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.add(new JLabel("  Logged in as: admin (Administrator)"), BorderLayout.WEST);
        JButton logoutBtn = new JButton("Logout");
        topBar.add(logoutBtn, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("User Management", buildUserManagementPanel());
        tabs.addTab("Quiz Content Management", buildQuizContentPanel());
        tabs.addTab("System Settings", buildSystemSettingsPanel());
        tabs.addTab("Performance Reports", buildPerformanceReportsPanel());
        tabs.addTab("System Alerts", buildAlertsPanel());

        tabs.addChangeListener(e -> {
            int idx = tabs.getSelectedIndex();
            if (idx == 0) refreshUsersTable();
            if (idx == 1) refreshQuizzesTable();
            if (idx == 3) refreshChart();
            if (idx == 4) refreshAlertsTable();
        });

        add(tabs, BorderLayout.CENTER);

        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });
    }

    // ==================== TAB 1: User Management ====================

    private JPanel buildUserManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nameField = new JTextField(14);
        JTextField emailField = new JTextField(14);
        JTextField usernameField = new JTextField(12);
        JTextField passwordField = new JTextField(12);
        JComboBox<String> roleBox = new JComboBox<>(new String[]{"PARTICIPANT", "QUIZ_CREATOR", "ADMIN"});

        int row = 0, col = 0;
        gbc.gridx = col++; gbc.gridy = row; formPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = col++; formPanel.add(nameField, gbc);
        gbc.gridx = col++; formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = col++; formPanel.add(emailField, gbc);
        row++; col = 0;
        gbc.gridx = col++; gbc.gridy = row; formPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = col++; formPanel.add(usernameField, gbc);
        gbc.gridx = col++; formPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = col++; formPanel.add(passwordField, gbc);
        row++; col = 0;
        gbc.gridx = col++; gbc.gridy = row; formPanel.add(new JLabel("Role:"), gbc);
        gbc.gridx = col++; formPanel.add(roleBox, gbc);

        JButton addBtn = new JButton("Create User");
        gbc.gridx = col; formPanel.add(addBtn, gbc);

        panel.add(formPanel, BorderLayout.NORTH);

        usersModel = new DefaultTableModel(new String[]{"Username", "Name", "Email", "Role"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        usersTable = new JTable(usersModel);
        panel.add(new JScrollPane(usersTable), BorderLayout.CENTER);

        JPanel bottomBtns = new JPanel();
        JButton deleteBtn = new JButton("Delete Selected User");
        JButton changeRoleBtn = new JButton("Change Role of Selected");
        bottomBtns.add(deleteBtn);
        bottomBtns.add(changeRoleBtn);
        panel.add(bottomBtns, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> {
            String name = DataStore.sanitize(nameField.getText());
            String email = DataStore.sanitize(emailField.getText());
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();
            String role = (String) roleBox.getSelectedItem();

            if (name.isEmpty() || username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name, username and password are required.");
                return;
            }
            if (DataStore.userExists(username)) {
                JOptionPane.showMessageDialog(this, "Username already exists.");
                return;
            }
            DataStore.addUser(User.create(role, username, password, name, email));
            JOptionPane.showMessageDialog(this, "User \"" + username + "\" created successfully.");
            nameField.setText(""); emailField.setText(""); usernameField.setText(""); passwordField.setText("");
            refreshUsersTable();
        });

        deleteBtn.addActionListener(e -> {
            int r = usersTable.getSelectedRow();
            if (r == -1) { JOptionPane.showMessageDialog(this, "Select a user first."); return; }
            String username = (String) usersModel.getValueAt(r, 0);
            if (username.equals("admin")) { JOptionPane.showMessageDialog(this, "Cannot delete the primary admin account."); return; }
            int confirm = JOptionPane.showConfirmDialog(this, "Delete user \"" + username + "\"?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                DataStore.deleteUser(username);
                JOptionPane.showMessageDialog(this, "User deleted successfully.");
                refreshUsersTable();
            }
        });

        changeRoleBtn.addActionListener(e -> {
            int r = usersTable.getSelectedRow();
            if (r == -1) { JOptionPane.showMessageDialog(this, "Select a user first."); return; }
            String username = (String) usersModel.getValueAt(r, 0);
            String[] roles = {"PARTICIPANT", "QUIZ_CREATOR", "ADMIN"};
            String newRole = (String) JOptionPane.showInputDialog(this, "New role for " + username + ":",
                    "Change Role", JOptionPane.PLAIN_MESSAGE, null, roles, roles[0]);
            if (newRole != null) {
                DataStore.updateUserRole(username, newRole);
                JOptionPane.showMessageDialog(this, "Role updated successfully.");
                refreshUsersTable();
            }
        });

        refreshUsersTable();
        return panel;
    }

    private void refreshUsersTable() {
        usersModel.setRowCount(0);
        for (User u : DataStore.loadUsers()) {
            usersModel.addRow(new Object[]{u.getUsername(), u.getFullName(), u.getEmail(), u.getRole()});
        }
    }

    // ==================== TAB 2: Quiz Content Management ====================

    private JPanel buildQuizContentPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JLabel info = new JLabel("  Approve or reject quiz content submitted by Quiz Creators.");
        panel.add(info, BorderLayout.NORTH);

        quizzesModel = new DefaultTableModel(new String[]{"ID", "Title", "Creator", "Questions", "Time (min)", "Status"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        quizzesTable = new JTable(quizzesModel);
        panel.add(new JScrollPane(quizzesTable), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        JButton approveBtn = new JButton("Approve Selected");
        JButton rejectBtn = new JButton("Reject Selected");
        JButton deleteBtn = new JButton("Delete Selected Quiz");
        btnPanel.add(approveBtn);
        btnPanel.add(rejectBtn);
        btnPanel.add(deleteBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        approveBtn.addActionListener(e -> {
            int r = quizzesTable.getSelectedRow();
            if (r == -1) { JOptionPane.showMessageDialog(this, "Select a quiz first."); return; }
            int id = (int) quizzesModel.getValueAt(r, 0);
            String title = (String) quizzesModel.getValueAt(r, 1);
            String creator = (String) quizzesModel.getValueAt(r, 2);
            DataStore.updateQuizStatus(id, "APPROVED");
            DataStore.addAlert("Quiz \"" + title + "\" by " + creator + " was APPROVED.");
            JOptionPane.showMessageDialog(this, "Content approval status: APPROVED.");
            refreshQuizzesTable();
        });

        rejectBtn.addActionListener(e -> {
            int r = quizzesTable.getSelectedRow();
            if (r == -1) { JOptionPane.showMessageDialog(this, "Select a quiz first."); return; }
            int id = (int) quizzesModel.getValueAt(r, 0);
            String title = (String) quizzesModel.getValueAt(r, 1);
            String creator = (String) quizzesModel.getValueAt(r, 2);
            DataStore.updateQuizStatus(id, "REJECTED");
            DataStore.addAlert("Quiz \"" + title + "\" by " + creator + " was REJECTED.");
            JOptionPane.showMessageDialog(this, "Content approval status: REJECTED.");
            refreshQuizzesTable();
        });

        deleteBtn.addActionListener(e -> {
            int r = quizzesTable.getSelectedRow();
            if (r == -1) { JOptionPane.showMessageDialog(this, "Select a quiz first."); return; }
            int id = (int) quizzesModel.getValueAt(r, 0);
            String title = (String) quizzesModel.getValueAt(r, 1);
            int confirm = JOptionPane.showConfirmDialog(this, "Permanently delete quiz \"" + title + "\" and its questions?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                DataStore.deleteQuiz(id);
                refreshQuizzesTable();
            }
        });

        refreshQuizzesTable();
        return panel;
    }

    private void refreshQuizzesTable() {
        if (quizzesModel == null) return;
        quizzesModel.setRowCount(0);
        for (Quiz q : DataStore.loadQuizzes()) {
            int count = DataStore.loadQuestionsForQuiz(q.getId()).size();
            quizzesModel.addRow(new Object[]{q.getId(), q.getTitle(), q.getCreatorUsername(), count, q.getTimeLimitMinutes(), q.getStatus()});
        }
    }

    // ==================== TAB 3: System Settings ====================

    private JPanel buildSystemSettingsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Settings current = DataStore.loadSettings();

        JTextField platformNameField = new JTextField(current.getPlatformName(), 20);
        JSpinner defaultTimeSpinner = new JSpinner(new SpinnerNumberModel(current.getDefaultTimeLimitMinutes(), 1, 120, 1));
        JSpinner passPctSpinner = new JSpinner(new SpinnerNumberModel(current.getPassingPercentage(), 0, 100, 5));

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; panel.add(new JLabel("Platform Name:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; panel.add(platformNameField, gbc);

        gbc.gridx = 0; gbc.gridy = row; panel.add(new JLabel("Default Quiz Time Limit (min):"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; panel.add(defaultTimeSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = row; panel.add(new JLabel("Passing Percentage:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; panel.add(passPctSpinner, gbc);

        JButton saveBtn = new JButton("Save Settings");
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        panel.add(saveBtn, gbc);

        saveBtn.addActionListener(e -> {
            String name = DataStore.sanitize(platformNameField.getText());
            if (name.isEmpty()) { JOptionPane.showMessageDialog(this, "Platform name cannot be empty."); return; }
            Settings updated = new Settings(name, (Integer) defaultTimeSpinner.getValue(), (Integer) passPctSpinner.getValue());
            DataStore.saveSettings(updated);
            JOptionPane.showMessageDialog(this, "Settings updated successfully.");
        });

        return panel;
    }

    // ==================== TAB 4: Performance Reports ====================

    private JPanel buildPerformanceReportsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("  Average score (%) per quiz, across all participants:"), BorderLayout.NORTH);

        chartPanel = new PerformanceChartPanel(java.util.Collections.emptyList(), java.util.Collections.emptyList());
        panel.add(chartPanel, BorderLayout.CENTER);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refreshChart());
        panel.add(refreshBtn, BorderLayout.SOUTH);

        refreshChart();
        return panel;
    }

    /**
     * Loads the chart's data on a background thread (SwingWorker) so the
     * two DB queries involved never freeze the Event Dispatch Thread,
     * even as the results table grows. doInBackground() runs off the
     * EDT; done() hops back onto the EDT to safely touch the Swing
     * component, which is the standard safe pattern for multithreaded
     * Swing UI updates.
     */
    private void refreshChart() {
        if (chartPanel == null) return;
        new SwingWorker<Object[], Void>() {
            @Override
            protected Object[] doInBackground() {
                List<Quiz> quizzes = DataStore.loadQuizzes();
                List<Result> allResults = DataStore.loadResults();
                java.util.List<String> labels = new java.util.ArrayList<>();
                java.util.List<Integer> values = new java.util.ArrayList<>();
                for (Quiz q : quizzes) {
                    java.util.List<Result> results = new java.util.ArrayList<>();
                    for (Result r : allResults) if (r.getQuizId() == q.getId()) results.add(r);
                    if (results.isEmpty()) continue;
                    int total = 0;
                    for (Result r : results) total += r.getPercentage();
                    labels.add(q.getTitle());
                    values.add(total / results.size());
                }
                return new Object[]{labels, values};
            }

            @Override
            @SuppressWarnings("unchecked")
            protected void done() {
                try {
                    Object[] data = get();
                    chartPanel.updateData((java.util.List<String>) data[0], (java.util.List<Integer>) data[1]);
                } catch (Exception e) {
                    System.out.println("Chart refresh failed: " + e.getMessage());
                }
            }
        }.execute();
    }

    // ==================== TAB 5: System Alerts ====================

    private JPanel buildAlertsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        alertsModel = new DefaultTableModel(new String[]{"Alert", "Date"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable alertsTable = new JTable(alertsModel);
        panel.add(new JScrollPane(alertsTable), BorderLayout.CENTER);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refreshAlertsTable());
        panel.add(refreshBtn, BorderLayout.SOUTH);

        refreshAlertsTable();
        return panel;
    }

    private void refreshAlertsTable() {
        if (alertsModel == null) return;
        alertsModel.setRowCount(0);
        List<Alert> alerts = DataStore.loadAlerts();
        // newest first
        for (int i = alerts.size() - 1; i >= 0; i--) {
            Alert a = alerts.get(i);
            alertsModel.addRow(new Object[]{a.getMessage(), a.getDate()});
        }
    }
}
