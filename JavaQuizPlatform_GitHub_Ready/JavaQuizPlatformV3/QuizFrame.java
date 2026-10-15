import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * QuizFrame.java
 * The screen where a student actually attempts one specific quiz.
 *   - Shows every question that belongs to the chosen Quiz (order
 *     shuffled each attempt).
 *   - Runs a single countdown timer for the WHOLE quiz, based on the
 *     quiz's own time limit (set by the Admin when creating it), using
 *     javax.swing.Timer firing once every second.
 *   - Lets the student move Previous/Next between questions before
 *     submitting; their choice for each question is remembered.
 *   - On Submit (or when time runs out) it compares each selected
 *     answer to the correct answer, computes the score, and saves a
 *     Result (tagged with this quiz's id + title) via DataStore.
 */
public class QuizFrame extends JFrame {
    private Quiz quiz;
    private List<Question> questions;
    private int currentIndex = 0;
    private Map<Integer, Character> selectedAnswers = new HashMap<>();

    private JLabel questionLabel;
    private JRadioButton optionA, optionB, optionC, optionD;
    private ButtonGroup optionsGroup;
    private JLabel timerLabel;
    private JLabel progressLabel;
    private javax.swing.Timer swingTimer;
    private int timeLeftSeconds;

    private User currentUser;
    private ParticipantDashboard parentDashboard;

    public QuizFrame(User user, Quiz quiz, List<Question> questions, ParticipantDashboard parent) {
        this.currentUser = user;
        this.quiz = quiz;
        this.parentDashboard = parent;

        this.questions = new ArrayList<>(questions);
        Collections.shuffle(this.questions);

        setTitle("Quiz: " + quiz.getTitle());
        setSize(620, 420);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // must Submit to leave
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new BorderLayout());
        progressLabel = new JLabel();
        timerLabel = new JLabel("", SwingConstants.RIGHT);
        timerLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        timerLabel.setForeground(Color.RED);
        topPanel.add(progressLabel, BorderLayout.WEST);
        topPanel.add(timerLabel, BorderLayout.EAST);
        topPanel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        add(topPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        questionLabel = new JLabel();
        questionLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        centerPanel.add(questionLabel);
        centerPanel.add(Box.createVerticalStrut(15));

        optionA = new JRadioButton();
        optionB = new JRadioButton();
        optionC = new JRadioButton();
        optionD = new JRadioButton();
        optionsGroup = new ButtonGroup();
        optionsGroup.add(optionA);
        optionsGroup.add(optionB);
        optionsGroup.add(optionC);
        optionsGroup.add(optionD);
        centerPanel.add(optionA);
        centerPanel.add(optionB);
        centerPanel.add(optionC);
        centerPanel.add(optionD);

        add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        JButton prevBtn = new JButton("Previous");
        JButton nextBtn = new JButton("Next");
        JButton submitBtn = new JButton("Submit Quiz");
        bottomPanel.add(prevBtn);
        bottomPanel.add(nextBtn);
        bottomPanel.add(submitBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        prevBtn.addActionListener(e -> {
            saveCurrentAnswer();
            if (currentIndex > 0) {
                currentIndex--;
                showQuestion();
            }
        });
        nextBtn.addActionListener(e -> {
            saveCurrentAnswer();
            if (currentIndex < this.questions.size() - 1) {
                currentIndex++;
                showQuestion();
            }
        });
        submitBtn.addActionListener(e -> {
            saveCurrentAnswer();
            confirmAndFinish();
        });

        timeLeftSeconds = Math.max(quiz.getTimeLimitMinutes(), 1) * 60;
        swingTimer = new javax.swing.Timer(1000, e -> {
            timeLeftSeconds--;
            updateTimerLabel();
            if (timeLeftSeconds <= 0) {
                swingTimer.stop();
                saveCurrentAnswer();
                JOptionPane.showMessageDialog(this, "Time's up! Submitting your quiz.");
                finishQuiz();
            }
        });
        swingTimer.start();

        showQuestion();
    }

    private void updateTimerLabel() {
        int minutes = timeLeftSeconds / 60;
        int seconds = timeLeftSeconds % 60;
        timerLabel.setText(String.format("Time Left: %02d:%02d", minutes, seconds));
    }

    private void showQuestion() {
        Question q = questions.get(currentIndex);
        progressLabel.setText("Question " + (currentIndex + 1) + " of " + questions.size());
        questionLabel.setText("<html><body style='width: 440px'>" + (currentIndex + 1) + ". " + q.getQuestionText() + "</body></html>");
        optionA.setText("A) " + q.getOptionA());
        optionB.setText("B) " + q.getOptionB());
        optionC.setText("C) " + q.getOptionC());
        optionD.setText("D) " + q.getOptionD());

        optionsGroup.clearSelection();
        Character prevAnswer = selectedAnswers.get(q.getId());
        if (prevAnswer != null) {
            switch (prevAnswer) {
                case 'A': optionA.setSelected(true); break;
                case 'B': optionB.setSelected(true); break;
                case 'C': optionC.setSelected(true); break;
                case 'D': optionD.setSelected(true); break;
            }
        }
    }

    private void saveCurrentAnswer() {
        Question q = questions.get(currentIndex);
        char selected = ' ';
        if (optionA.isSelected()) selected = 'A';
        else if (optionB.isSelected()) selected = 'B';
        else if (optionC.isSelected()) selected = 'C';
        else if (optionD.isSelected()) selected = 'D';
        if (selected != ' ') {
            selectedAnswers.put(q.getId(), selected);
        }
    }

    private void confirmAndFinish() {
        int unanswered = questions.size() - selectedAnswers.size();
        if (unanswered > 0) {
            int choice = JOptionPane.showConfirmDialog(this,
                    unanswered + " question(s) are still unanswered. Submit anyway?",
                    "Confirm Submit", JOptionPane.YES_NO_OPTION);
            if (choice != JOptionPane.YES_OPTION) return;
        }
        finishQuiz();
    }

    private void finishQuiz() {
        if (swingTimer.isRunning()) swingTimer.stop();

        int score = 0;
        for (Question q : questions) {
            Character ans = selectedAnswers.get(q.getId());
            if (ans != null && ans == q.getCorrectOption()) {
                score++;
            }
        }

        String date = new SimpleDateFormat("dd-MM-yyyy HH:mm").format(new Date());
        DataStore.addResult(new Result(currentUser.getUsername(), quiz.getId(), quiz.getTitle(), score, questions.size(), date, ""));

        int pct = questions.isEmpty() ? 0 : Math.round((score * 100f) / questions.size());
        JOptionPane.showMessageDialog(this,
                "Quiz Finished!\n\"" + quiz.getTitle() + "\"\nYour Score: " + score + " / " + questions.size() + " (" + pct + "%)",
                "Result", JOptionPane.INFORMATION_MESSAGE);

        dispose();
        parentDashboard.dispose();
        // Rebuild the dashboard so the "My History" tab shows this new attempt.
        new ParticipantDashboard(currentUser).setVisible(true);
    }
}
