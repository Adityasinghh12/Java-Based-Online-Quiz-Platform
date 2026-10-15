import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * QuizCreatorDashboard.java
 * Screen shown after a Quiz Creator logs in. Five tabs:
 *   1. Quiz Creation          - create quizzes (submitted for Admin approval) and add questions
 *   2. Quiz Results           - review participant results on their quizzes and leave feedback/grade
 *   3. Participant Interactions - message participants who took their quizzes
 *   4. Quiz History           - log of every quiz they have created, with approval status
 *   5. Performance Overview   - summary stats + chart of their quizzes' performance
 */
public class QuizCreatorDashboard extends JFrame {
    private User currentUser;

    private JComboBox<Quiz> addQuestionQuizBox;
    private DefaultTableModel myQuizzesModel;

    private DefaultTableModel resultsModel;
    private java.util.List<Result> currentResultsList;

    private DefaultTableModel historyModel;

    private PerformanceChartPanel chartPanel;
    private JLabel statsLabel;

    private DefaultTableModel contactsModel;
    private JTextArea conversationArea;
    private String selectedContact = null;

    public QuizCreatorDashboard(User user) {
        this.currentUser = user;
        setTitle("Quiz Creator Dashboard - " + user.getFullName());
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.add(new JLabel("  Logged in as: " + user.getFullName() + " (Quiz Creator)"), BorderLayout.WEST);
        JButton logoutBtn = new JButton("Logout");
        topBar.add(logoutBtn, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Quiz Creation", buildQuizCreationPanel());
        tabs.addTab("Quiz Results", buildQuizResultsPanel());
        tabs.addTab("Participant Interactions", buildInteractionsPanel());
        tabs.addTab("Quiz History", buildHistoryPanel());
        tabs.addTab("Performance Overview", buildPerformanceOverviewPanel());

        tabs.addChangeListener(e -> {
            int idx = tabs.getSelectedIndex();
            if (idx == 0) { refreshMyQuizzesTable(); refreshQuizComboBox(); }
            if (idx == 1) refreshResultsTable();
            if (idx == 2) refreshContactsTable();
            if (idx == 3) refreshHistoryTable();
            if (idx == 4) refreshPerformanceOverview();
        });

        add(tabs, BorderLayout.CENTER);

        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });
    }

    // ==================== TAB 1: Quiz Creation ====================

    private JPanel buildQuizCreationPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel top = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField titleField = new JTextField(18);
        JTextField descField = new JTextField(18);
        Settings settings = DataStore.loadSettings();
        JSpinner timeSpinner = new JSpinner(new SpinnerNumberModel(settings.getDefaultTimeLimitMinutes(), 1, 120, 1));
        JButton createQuizBtn = new JButton("Submit New Quiz for Approval");

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; top.add(new JLabel("New Quiz Title:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; top.add(titleField, gbc);
        gbc.gridx = 0; gbc.gridy = row; top.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; top.add(descField, gbc);
        gbc.gridx = 0; gbc.gridy = row; top.add(new JLabel("Time Limit (min):"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; top.add(timeSpinner, gbc);
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; top.add(createQuizBtn, gbc);

        createQuizBtn.addActionListener(e -> {
            String title = DataStore.sanitize(titleField.getText());
            String desc = DataStore.sanitize(descField.getText());
            if (title.isEmpty()) { JOptionPane.showMessageDialog(this, "Quiz title cannot be empty."); return; }
            int newId = DataStore.getNextQuizId();
            DataStore.addQuiz(new Quiz(newId, title, desc, (Integer) timeSpinner.getValue(), currentUser.getUsername(), "PENDING"));
            DataStore.addAlert("New quiz \"" + title + "\" submitted by " + currentUser.getUsername() + " - awaiting approval.");
            JOptionPane.showMessageDialog(this, "Quiz created successfully! It is now PENDING Admin approval.\nAdd questions to it below.");
            titleField.setText(""); descField.setText("");
            refreshMyQuizzesTable();
            refreshQuizComboBox();
        });

        panel.add(top, BorderLayout.NORTH);

        // My quizzes table (quick reference)
        myQuizzesModel = new DefaultTableModel(new String[]{"ID", "Title", "Status", "Questions"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable myQuizzesTable = new JTable(myQuizzesModel);

        // Add question sub-panel
        JPanel addQPanel = new JPanel(new GridBagLayout());
        GridBagConstraints g2 = new GridBagConstraints();
        g2.insets = new Insets(4, 4, 4, 4);
        g2.fill = GridBagConstraints.HORIZONTAL;

        addQuestionQuizBox = new JComboBox<>();
        JTextField qField = new JTextField(22);
        JTextField aField = new JTextField(16), bField = new JTextField(16), cField = new JTextField(16), dField = new JTextField(16);
        JComboBox<String> correctBox = new JComboBox<>(new String[]{"A", "B", "C", "D"});
        JButton addQBtn = new JButton("Add Question to Selected Quiz");

        int r2 = 0;
        g2.gridx = 0; g2.gridy = r2; addQPanel.add(new JLabel("Quiz:"), g2);
        g2.gridx = 1; g2.gridy = r2++; addQPanel.add(addQuestionQuizBox, g2);
        g2.gridx = 0; g2.gridy = r2; addQPanel.add(new JLabel("Question:"), g2);
        g2.gridx = 1; g2.gridy = r2++; addQPanel.add(qField, g2);
        g2.gridx = 0; g2.gridy = r2; addQPanel.add(new JLabel("Option A:"), g2);
        g2.gridx = 1; g2.gridy = r2++; addQPanel.add(aField, g2);
        g2.gridx = 0; g2.gridy = r2; addQPanel.add(new JLabel("Option B:"), g2);
        g2.gridx = 1; g2.gridy = r2++; addQPanel.add(bField, g2);
        g2.gridx = 0; g2.gridy = r2; addQPanel.add(new JLabel("Option C:"), g2);
        g2.gridx = 1; g2.gridy = r2++; addQPanel.add(cField, g2);
        g2.gridx = 0; g2.gridy = r2; addQPanel.add(new JLabel("Option D:"), g2);
        g2.gridx = 1; g2.gridy = r2++; addQPanel.add(dField, g2);
        g2.gridx = 0; g2.gridy = r2; addQPanel.add(new JLabel("Correct:"), g2);
        g2.gridx = 1; g2.gridy = r2++; addQPanel.add(correctBox, g2);
        g2.gridx = 0; g2.gridy = r2; g2.gridwidth = 2; addQPanel.add(addQBtn, g2);

        addQBtn.addActionListener(e -> {
            Quiz selected = (Quiz) addQuestionQuizBox.getSelectedItem();
            if (selected == null) { JOptionPane.showMessageDialog(this, "Create a quiz first."); return; }
            String q = DataStore.sanitize(qField.getText());
            String a = DataStore.sanitize(aField.getText());
            String b = DataStore.sanitize(bField.getText());
            String c = DataStore.sanitize(cField.getText());
            String d = DataStore.sanitize(dField.getText());
            if (q.isEmpty() || a.isEmpty() || b.isEmpty() || c.isEmpty() || d.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields."); return;
            }
            char correct = correctBox.getSelectedItem().toString().charAt(0);
            int newId = DataStore.getNextQuestionId();
            DataStore.addQuestion(new Question(newId, selected.getId(), q, a, b, c, d, correct));
            JOptionPane.showMessageDialog(this, "Question added to \"" + selected.getTitle() + "\".");
            qField.setText(""); aField.setText(""); bField.setText(""); cField.setText(""); dField.setText("");
            refreshMyQuizzesTable();
        });

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(myQuizzesTable), addQPanel);
        split.setResizeWeight(0.35);
        panel.add(split, BorderLayout.CENTER);

        refreshMyQuizzesTable();
        refreshQuizComboBox();
        return panel;
    }

    private void refreshMyQuizzesTable() {
        if (myQuizzesModel == null) return;
        myQuizzesModel.setRowCount(0);
        for (Quiz q : DataStore.loadQuizzesByCreator(currentUser.getUsername())) {
            int count = DataStore.loadQuestionsForQuiz(q.getId()).size();
            myQuizzesModel.addRow(new Object[]{q.getId(), q.getTitle(), q.getStatus(), count});
        }
    }

    private void refreshQuizComboBox() {
        if (addQuestionQuizBox == null) return;
        Object prev = addQuestionQuizBox.getSelectedItem();
        addQuestionQuizBox.removeAllItems();
        for (Quiz q : DataStore.loadQuizzesByCreator(currentUser.getUsername())) addQuestionQuizBox.addItem(q);
        if (prev != null) addQuestionQuizBox.setSelectedItem(prev);
    }

    // ==================== TAB 2: Quiz Results ====================

    private JPanel buildQuizResultsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("  Results from participants who attempted your quizzes. Select a row to leave feedback."), BorderLayout.NORTH);

        resultsModel = new DefaultTableModel(new String[]{"Participant", "Quiz", "Score", "%", "Date", "Feedback"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable resultsTable = new JTable(resultsModel);
        panel.add(new JScrollPane(resultsTable), BorderLayout.CENTER);

        JPanel feedbackPanel = new JPanel(new BorderLayout());
        JTextField feedbackField = new JTextField();
        JButton saveFeedbackBtn = new JButton("Save Feedback / Grade");
        feedbackPanel.add(feedbackField, BorderLayout.CENTER);
        feedbackPanel.add(saveFeedbackBtn, BorderLayout.EAST);
        panel.add(feedbackPanel, BorderLayout.SOUTH);

        saveFeedbackBtn.addActionListener(e -> {
            int r = resultsTable.getSelectedRow();
            if (r == -1 || currentResultsList == null || r >= currentResultsList.size()) {
                JOptionPane.showMessageDialog(this, "Select a result row first.");
                return;
            }
            Result target = currentResultsList.get(r);
            String feedback = DataStore.sanitize(feedbackField.getText());
            DataStore.updateResultFeedback(target.getUsername(), target.getQuizId(), target.getDate(), feedback);
            JOptionPane.showMessageDialog(this, "Feedback saved.");
            feedbackField.setText("");
            refreshResultsTable();
        });

        refreshResultsTable();
        return panel;
    }

    private void refreshResultsTable() {
        if (resultsModel == null) return;
        resultsModel.setRowCount(0);
        currentResultsList = DataStore.loadResultsForCreator(currentUser.getUsername());
        for (Result r : currentResultsList) {
            resultsModel.addRow(new Object[]{r.getUsername(), r.getQuizTitle(), r.getScore(), r.getPercentage() + "%", r.getDate(), r.getFeedback()});
        }
    }

    // ==================== TAB 3: Participant Interactions ====================

    private JPanel buildInteractionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        contactsModel = new DefaultTableModel(new String[]{"Participant"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable contactsTable = new JTable(contactsModel);
        JScrollPane contactsScroll = new JScrollPane(contactsTable);
        contactsScroll.setPreferredSize(new Dimension(180, 0));

        conversationArea = new JTextArea();
        conversationArea.setEditable(false);
        conversationArea.setLineWrap(true);
        conversationArea.setWrapStyleWord(true);
        JScrollPane convoScroll = new JScrollPane(conversationArea);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(convoScroll, BorderLayout.CENTER);

        JPanel replyPanel = new JPanel(new BorderLayout());
        JTextField replyField = new JTextField();
        JButton sendBtn = new JButton("Send Reply");
        replyPanel.add(replyField, BorderLayout.CENTER);
        replyPanel.add(sendBtn, BorderLayout.EAST);
        centerPanel.add(replyPanel, BorderLayout.SOUTH);

        panel.add(contactsScroll, BorderLayout.WEST);
        panel.add(centerPanel, BorderLayout.CENTER);

        contactsTable.getSelectionModel().addListSelectionListener(e -> {
            int r = contactsTable.getSelectedRow();
            if (r != -1) {
                selectedContact = (String) contactsModel.getValueAt(r, 0);
                loadConversation();
            }
        });

        sendBtn.addActionListener(e -> {
            if (selectedContact == null) { JOptionPane.showMessageDialog(this, "Select a participant first."); return; }
            String text = DataStore.sanitize(replyField.getText());
            if (text.isEmpty()) return;
            int newId = DataStore.getNextMessageId();
            String date = new java.text.SimpleDateFormat("dd-MM-yyyy HH:mm").format(new java.util.Date());
            DataStore.addMessage(new Message(newId, currentUser.getUsername(), selectedContact, -1, text, date));
            replyField.setText("");
            loadConversation();
        });

        refreshContactsTable();
        return panel;
    }

    private void refreshContactsTable() {
        if (contactsModel == null) return;
        contactsModel.setRowCount(0);
        for (String contact : DataStore.loadContactsFor(currentUser.getUsername())) {
            contactsModel.addRow(new Object[]{contact});
        }
    }

    private void loadConversation() {
        if (selectedContact == null) return;
        StringBuilder sb = new StringBuilder();
        for (Message m : DataStore.loadConversation(currentUser.getUsername(), selectedContact)) {
            String who = m.getFromUsername().equals(currentUser.getUsername()) ? "You" : m.getFromUsername();
            sb.append("[").append(m.getDate()).append("] ").append(who).append(": ").append(m.getText()).append("\n\n");
        }
        conversationArea.setText(sb.toString());
    }

    // ==================== TAB 4: Quiz History ====================

    private JPanel buildHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        historyModel = new DefaultTableModel(new String[]{"ID", "Title", "Description", "Time (min)", "Status"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable historyTable = new JTable(historyModel);
        panel.add(new JScrollPane(historyTable), BorderLayout.CENTER);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refreshHistoryTable());
        panel.add(refreshBtn, BorderLayout.SOUTH);

        refreshHistoryTable();
        return panel;
    }

    private void refreshHistoryTable() {
        if (historyModel == null) return;
        historyModel.setRowCount(0);
        for (Quiz q : DataStore.loadQuizzesByCreator(currentUser.getUsername())) {
            historyModel.addRow(new Object[]{q.getId(), q.getTitle(), q.getDescription(), q.getTimeLimitMinutes(), q.getStatus()});
        }
    }

    // ==================== TAB 5: Performance Overview ====================

    private JPanel buildPerformanceOverviewPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        statsLabel = new JLabel();
        statsLabel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panel.add(statsLabel, BorderLayout.NORTH);

        chartPanel = new PerformanceChartPanel(java.util.Collections.emptyList(), java.util.Collections.emptyList());
        panel.add(chartPanel, BorderLayout.CENTER);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refreshPerformanceOverview());
        panel.add(refreshBtn, BorderLayout.SOUTH);

        refreshPerformanceOverview();
        return panel;
    }

    /** Same background-thread pattern as AdminDashboard's refreshChart():
     *  the DB reads happen off the EDT in doInBackground(), and only the
     *  final Swing update happens on the EDT inside done(). */
    private void refreshPerformanceOverview() {
        if (chartPanel == null) return;
        new SwingWorker<Object[], Void>() {
            @Override
            protected Object[] doInBackground() {
                List<Quiz> myQuizzes = DataStore.loadQuizzesByCreator(currentUser.getUsername());
                List<Result> myResults = DataStore.loadResultsForCreator(currentUser.getUsername());

                int totalAttempts = myResults.size();
                double avgPct = 0;
                if (totalAttempts > 0) {
                    int sum = 0;
                    for (Result r : myResults) sum += r.getPercentage();
                    avgPct = sum / (double) totalAttempts;
                }
                String stats = String.format(
                        "<html>Total Quizzes Created: <b>%d</b> &nbsp;|&nbsp; Total Attempts Received: <b>%d</b> &nbsp;|&nbsp; Average Score: <b>%.1f%%</b></html>",
                        myQuizzes.size(), totalAttempts, avgPct);

                java.util.List<String> labels = new java.util.ArrayList<>();
                java.util.List<Integer> values = new java.util.ArrayList<>();
                for (Quiz q : myQuizzes) {
                    java.util.List<Result> results = new java.util.ArrayList<>();
                    for (Result r : myResults) if (r.getQuizId() == q.getId()) results.add(r);
                    if (results.isEmpty()) continue;
                    int sum = 0;
                    for (Result r : results) sum += r.getPercentage();
                    labels.add(q.getTitle());
                    values.add(sum / results.size());
                }
                return new Object[]{stats, labels, values};
            }

            @Override
            @SuppressWarnings("unchecked")
            protected void done() {
                try {
                    Object[] data = get();
                    statsLabel.setText((String) data[0]);
                    chartPanel.updateData((java.util.List<String>) data[1], (java.util.List<Integer>) data[2]);
                } catch (Exception e) {
                    System.out.println("Performance overview refresh failed: " + e.getMessage());
                }
            }
        }.execute();
    }
}
