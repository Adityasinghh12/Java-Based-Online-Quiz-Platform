import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

/**
 * ParticipantDashboard.java
 * Screen shown after a Participant logs in. Six tabs:
 *   1. Available Quizzes        - browse APPROVED quizzes and start one (fulfils "Take Quizzes")
 *   2. Quiz Participation History - table of quizzes taken + results
 *   3. Performance Report       - detailed performance breakdown + pass/fail
 *   4. Interactions             - message the creator of a quiz they've taken
 *   5. Quiz Reminders           - simple self-set reminder notes
 *   6. Leaderboard              - overall ranking across all participants
 */
public class ParticipantDashboard extends JFrame {
    private User currentUser;

    private DefaultTableModel quizzesModel;
    private JTable quizzesTable;

    private DefaultTableModel reminderModel;
    private java.util.List<Reminder> currentReminders;

    private DefaultTableModel contactsModel;
    private JTextArea conversationArea;
    private String selectedContact = null;

    private DefaultTableModel leaderboardModel;

    public ParticipantDashboard(User user) {
        this.currentUser = user;
        setTitle("Participant Dashboard - " + user.getFullName());
        setSize(880, 580);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.add(new JLabel("  Logged in as: " + user.getFullName() + " (Participant)"), BorderLayout.WEST);
        JButton logoutBtn = new JButton("Logout");
        topBar.add(logoutBtn, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Available Quizzes", buildAvailableQuizzesPanel());
        tabs.addTab("Participation History", buildHistoryPanel());
        tabs.addTab("Performance Report", buildPerformanceReportPanel());
        tabs.addTab("Interactions", buildInteractionsPanel());
        tabs.addTab("Quiz Reminders", buildRemindersPanel());
        tabs.addTab("Leaderboard", buildLeaderboardPanel());

        tabs.addChangeListener(e -> {
            int idx = tabs.getSelectedIndex();
            if (idx == 0) refreshQuizzesTable();
            if (idx == 3) refreshContactsTable();
            if (idx == 4) refreshRemindersTable();
            if (idx == 5) refreshLeaderboard();
        });

        add(tabs, BorderLayout.CENTER);

        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });
    }

    // ==================== TAB 1: Available Quizzes (Take Quizzes) ====================

    private JPanel buildAvailableQuizzesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("  Approved quizzes ready to attempt:"), BorderLayout.NORTH);

        quizzesModel = new DefaultTableModel(new String[]{"ID", "Title", "Description", "Time Limit (min)", "Questions"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        quizzesTable = new JTable(quizzesModel);
        panel.add(new JScrollPane(quizzesTable), BorderLayout.CENTER);

        JButton startBtn = new JButton("Start Selected Quiz");
        startBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        panel.add(startBtn, BorderLayout.SOUTH);

        startBtn.addActionListener(e -> {
            int row = quizzesTable.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Select a quiz first."); return; }
            int quizId = (int) quizzesModel.getValueAt(row, 0);
            int questionCount = (int) quizzesModel.getValueAt(row, 4);
            if (questionCount == 0) { JOptionPane.showMessageDialog(this, "This quiz has no questions yet. Please pick another one."); return; }

            Quiz quiz = DataStore.findQuizById(quizId);
            List<Question> questions = DataStore.loadQuestionsForQuiz(quizId);

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Start \"" + quiz.getTitle() + "\"?\n" + questionCount + " question(s), "
                            + quiz.getTimeLimitMinutes() + " minute(s) total.\n" + quiz.getDescription(),
                    "Start Quiz", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;

            setVisible(false);
            new QuizFrame(currentUser, quiz, questions, this).setVisible(true);
        });

        refreshQuizzesTable();
        return panel;
    }

    private void refreshQuizzesTable() {
        if (quizzesModel == null) return;
        quizzesModel.setRowCount(0);
        for (Quiz q : DataStore.loadApprovedQuizzes()) {
            int count = DataStore.loadQuestionsForQuiz(q.getId()).size();
            quizzesModel.addRow(new Object[]{q.getId(), q.getTitle(), q.getDescription(), q.getTimeLimitMinutes(), count});
        }
    }

    // ==================== TAB 2: Quiz Participation History ====================

    private JPanel buildHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        DefaultTableModel model = new DefaultTableModel(new String[]{"Quiz", "Score", "Total", "%", "Date"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        Runnable load = () -> {
            model.setRowCount(0);
            for (Result r : DataStore.loadResultsForUser(currentUser.getUsername())) {
                model.addRow(new Object[]{r.getQuizTitle(), r.getScore(), r.getTotalQuestions(), r.getPercentage() + "%", r.getDate()});
            }
        };
        load.run();

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> load.run());
        panel.add(refreshBtn, BorderLayout.SOUTH);
        return panel;
    }

    // ==================== TAB 3: Performance Report ====================

    private JPanel buildPerformanceReportPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        DefaultTableModel model = new DefaultTableModel(new String[]{"Quiz", "Score", "%", "Result", "Creator Feedback", "Date"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JLabel summaryLabel = new JLabel();
        summaryLabel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panel.add(summaryLabel, BorderLayout.NORTH);

        Runnable load = () -> {
            model.setRowCount(0);
            Settings settings = DataStore.loadSettings();
            List<Result> results = DataStore.loadResultsForUser(currentUser.getUsername());
            int sum = 0;
            for (Result r : results) {
                String verdict = r.getPercentage() >= settings.getPassingPercentage() ? "PASS" : "FAIL";
                model.addRow(new Object[]{r.getQuizTitle(), r.getScore() + "/" + r.getTotalQuestions(),
                        r.getPercentage() + "%", verdict, r.getFeedback(), r.getDate()});
                sum += r.getPercentage();
            }
            double avg = results.isEmpty() ? 0 : sum / (double) results.size();
            summaryLabel.setText(String.format("<html>Quizzes Attempted: <b>%d</b> &nbsp;|&nbsp; Average Score: <b>%.1f%%</b> &nbsp;|&nbsp; Passing Mark: <b>%d%%</b></html>",
                    results.size(), avg, settings.getPassingPercentage()));
        };
        load.run();

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> load.run());
        panel.add(refreshBtn, BorderLayout.SOUTH);
        return panel;
    }

    // ==================== TAB 4: Interactions (with Quiz Creators) ====================

    private JPanel buildInteractionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        contactsModel = new DefaultTableModel(new String[]{"Quiz Creator"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable contactsTable = new JTable(contactsModel);
        JScrollPane contactsScroll = new JScrollPane(contactsTable);
        contactsScroll.setPreferredSize(new Dimension(180, 0));

        conversationArea = new JTextArea();
        conversationArea.setEditable(false);
        conversationArea.setLineWrap(true);
        conversationArea.setWrapStyleWord(true);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(new JScrollPane(conversationArea), BorderLayout.CENTER);

        // New-message row: pick any quiz's creator to start a fresh conversation
        JPanel newMsgPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JComboBox<Quiz> quizPicker = new JComboBox<>();
        for (Quiz q : DataStore.loadApprovedQuizzes()) quizPicker.addItem(q);
        gbc.gridx = 0; gbc.gridy = 0; newMsgPanel.add(new JLabel("Message creator of:"), gbc);
        gbc.gridx = 1; newMsgPanel.add(quizPicker, gbc);
        JButton startBtn = new JButton("Open Conversation");
        gbc.gridx = 2; newMsgPanel.add(startBtn, gbc);
        panel.add(newMsgPanel, BorderLayout.NORTH);

        startBtn.addActionListener(e -> {
            Quiz q = (Quiz) quizPicker.getSelectedItem();
            if (q == null) { JOptionPane.showMessageDialog(this, "No quizzes available to message about."); return; }
            selectedContact = q.getCreatorUsername();
            loadConversation();
            refreshContactsTable();
        });

        JPanel replyPanel = new JPanel(new BorderLayout());
        JTextField replyField = new JTextField();
        JButton sendBtn = new JButton("Send");
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
            if (selectedContact == null) { JOptionPane.showMessageDialog(this, "Pick a quiz creator to message first."); return; }
            String text = DataStore.sanitize(replyField.getText());
            if (text.isEmpty()) return;
            int newId = DataStore.getNextMessageId();
            String date = new java.text.SimpleDateFormat("dd-MM-yyyy HH:mm").format(new java.util.Date());
            DataStore.addMessage(new Message(newId, currentUser.getUsername(), selectedContact, -1, text, date));
            replyField.setText("");
            loadConversation();
            refreshContactsTable();
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

    // ==================== TAB 5: Quiz Reminders ====================

    private JPanel buildRemindersPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JComboBox<Quiz> quizBox = new JComboBox<>();
        for (Quiz q : DataStore.loadApprovedQuizzes()) quizBox.addItem(q);
        JTextField noteField = new JTextField(20);
        JButton addBtn = new JButton("Add Reminder");

        gbc.gridx = 0; gbc.gridy = 0; form.add(new JLabel("Quiz:"), gbc);
        gbc.gridx = 1; form.add(quizBox, gbc);
        gbc.gridx = 0; gbc.gridy = 1; form.add(new JLabel("Reminder Note:"), gbc);
        gbc.gridx = 1; form.add(noteField, gbc);
        gbc.gridx = 1; gbc.gridy = 2; form.add(addBtn, gbc);
        panel.add(form, BorderLayout.NORTH);

        reminderModel = new DefaultTableModel(new String[]{"Quiz", "Note", "Date Added"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable reminderTable = new JTable(reminderModel);
        panel.add(new JScrollPane(reminderTable), BorderLayout.CENTER);

        JButton deleteBtn = new JButton("Delete Selected Reminder");
        panel.add(deleteBtn, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> {
            Quiz q = (Quiz) quizBox.getSelectedItem();
            String note = DataStore.sanitize(noteField.getText());
            if (q == null || note.isEmpty()) { JOptionPane.showMessageDialog(this, "Select a quiz and enter a note."); return; }
            int newId = DataStore.getNextReminderId();
            String date = new java.text.SimpleDateFormat("dd-MM-yyyy HH:mm").format(new java.util.Date());
            DataStore.addReminder(new Reminder(newId, currentUser.getUsername(), q.getId(), q.getTitle(), note, date));
            noteField.setText("");
            refreshRemindersTable();
        });

        deleteBtn.addActionListener(e -> {
            int r = reminderTable.getSelectedRow();
            if (r == -1 || currentReminders == null || r >= currentReminders.size()) { JOptionPane.showMessageDialog(this, "Select a reminder first."); return; }
            DataStore.deleteReminder(currentReminders.get(r).getId());
            refreshRemindersTable();
        });

        refreshRemindersTable();
        return panel;
    }

    private void refreshRemindersTable() {
        if (reminderModel == null) return;
        reminderModel.setRowCount(0);
        currentReminders = DataStore.loadRemindersForUser(currentUser.getUsername());
        for (Reminder r : currentReminders) {
            reminderModel.addRow(new Object[]{r.getQuizTitle(), r.getNote(), r.getDate()});
        }
    }

    // ==================== TAB 6: Leaderboard ====================

    private JPanel buildLeaderboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("  Overall ranking by average score percentage across all quizzes taken:"), BorderLayout.NORTH);

        leaderboardModel = new DefaultTableModel(new String[]{"Rank", "Participant", "Attempts", "Avg %"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(leaderboardModel);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refreshLeaderboard());
        panel.add(refreshBtn, BorderLayout.SOUTH);

        refreshLeaderboard();
        return panel;
    }

    private void refreshLeaderboard() {
        if (leaderboardModel == null) return;
        leaderboardModel.setRowCount(0);

        Map<String, int[]> totals = new LinkedHashMap<>(); // username -> [sumPct, count]
        for (Result r : DataStore.loadResults()) {
            totals.putIfAbsent(r.getUsername(), new int[2]);
            totals.get(r.getUsername())[0] += r.getPercentage();
            totals.get(r.getUsername())[1] += 1;
        }

        List<Map.Entry<String, int[]>> entries = new java.util.ArrayList<>(totals.entrySet());
        entries.sort((e1, e2) -> {
            double avg1 = e1.getValue()[0] / (double) e1.getValue()[1];
            double avg2 = e2.getValue()[0] / (double) e2.getValue()[1];
            return Double.compare(avg2, avg1); // descending
        });

        int rank = 1;
        for (Map.Entry<String, int[]> entry : entries) {
            double avg = entry.getValue()[0] / (double) entry.getValue()[1];
            String name = entry.getKey();
            boolean isMe = name.equalsIgnoreCase(currentUser.getUsername());
            leaderboardModel.addRow(new Object[]{rank, name + (isMe ? "  (You)" : ""), entry.getValue()[1], String.format("%.1f%%", avg)});
            rank++;
        }
    }
}
