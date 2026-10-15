import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * DataStore.java
 * A thin facade in front of the 8 JDBC DAO classes. The UI (Login,
 * Register, the three Dashboards, QuizFrame) calls these static
 * methods without needing to know that, underneath, every one of
 * them is now a real SQL statement against the SQLite database via
 * JDBC - all persistence logic itself lives in the Dao classes.
 */
public class DataStore {
    private static final UserDao userDao = new UserDao();
    private static final QuizDao quizDao = new QuizDao();
    private static final QuestionDao questionDao = new QuestionDao();
    private static final ResultDao resultDao = new ResultDao();
    private static final MessageDao messageDao = new MessageDao();
    private static final AlertDao alertDao = new AlertDao();
    private static final ReminderDao reminderDao = new ReminderDao();
    private static final SettingsDao settingsDao = new SettingsDao();

    public static String sanitize(String text) {
        if (text == null) return "";
        return text.replace("|", "-").trim();
    }

    private static String now() {
        return new SimpleDateFormat("dd-MM-yyyy HH:mm").format(new Date());
    }

    /** Creates the schema + default Admin/Quiz Creator accounts and a starter quiz, if the DB is empty. */
    public static void initializeIfNeeded() {
        DBConnection.getConnection(); // triggers schema creation
        if (userDao.findAll().isEmpty()) {
            userDao.save(User.create("ADMIN", "admin", "admin123", "System Administrator", "admin@quizplatform.local"));
            userDao.save(User.create("QUIZ_CREATOR", "creator1", "creator123", "Demo Quiz Creator", "creator1@quizplatform.local"));

            Quiz starter = new Quiz(1, "Java Basics", "Core Java fundamentals - 10 questions", 5, "creator1", "APPROVED");
            quizDao.save(starter);

            String[][] qs = {
                {"Which keyword is used to inherit a class in Java?", "extends", "implements", "inherits", "super", "A"},
                {"Which of these is NOT a primitive data type in Java?", "int", "boolean", "String", "char", "C"},
                {"Which method is the entry point of a Java program?", "start()", "run()", "main()", "init()", "C"},
                {"Which collection class does not allow duplicate elements?", "ArrayList", "LinkedList", "HashSet", "Vector", "C"},
                {"What is the default value of a boolean variable in Java?", "true", "false", "0", "null", "B"},
                {"Which keyword is used to handle exceptions in Java?", "try-catch", "if-else", "switch-case", "for-loop", "A"},
                {"Which operator is used to compare two values in Java?", "=", "==", "===", "equals", "B"},
                {"Which of these is used to create an object in Java?", "new", "create", "make", "object", "A"},
                {"Which package contains the Scanner class?", "java.io", "java.util", "java.lang", "java.net", "B"},
                {"Which of these supports multiple inheritance in Java?", "Class", "Interface", "Abstract class", "Object", "B"},
            };
            int id = 1;
            for (String[] row : qs) {
                questionDao.save(new Question(id++, 1, row[0], row[1], row[2], row[3], row[4], row[5].charAt(0)));
            }
        }
        settingsDao.load(); // seeds default settings row if missing
    }

    // ---- Users ----
    public static List<User> loadUsers() { return userDao.findAll(); }
    public static void addUser(User user) { userDao.save(user); }
    public static User findUserByUsername(String username) { return userDao.findByUsername(username); }
    public static boolean userExists(String username) { return userDao.exists(username); }
    public static void deleteUser(String username) { userDao.delete(username); }
    public static void updateUserRole(String username, String newRole) { userDao.updateRole(username, newRole); }
    /** Throws AuthenticationException on bad credentials - callers must handle it. */
    public static User authenticate(String username, String password) throws AuthenticationException {
        return userDao.authenticate(username, password);
    }

    // ---- Quizzes ----
    public static List<Quiz> loadQuizzes() { return quizDao.findAll(); }
    public static void addQuiz(Quiz quiz) { quizDao.save(quiz); }
    public static int getNextQuizId() { return quizDao.getNextId(); }
    public static Quiz findQuizById(int quizId) { return quizDao.findById(quizId); }
    public static List<Quiz> loadQuizzesByCreator(String username) { return quizDao.findByCreator(username); }
    public static List<Quiz> loadApprovedQuizzes() { return quizDao.findByStatus("APPROVED"); }
    public static List<Quiz> loadPendingQuizzes() { return quizDao.findByStatus("PENDING"); }
    public static void updateQuizStatus(int quizId, String status) { quizDao.updateStatus(quizId, status); }
    public static void deleteQuiz(int quizId) {
        questionDao.deleteByQuizId(quizId); // cascading delete
        quizDao.delete(quizId);
    }

    // ---- Questions ----
    public static List<Question> loadQuestions() { return questionDao.findAll(); }
    public static List<Question> loadQuestionsForQuiz(int quizId) { return questionDao.findByQuizId(quizId); }
    public static int getNextQuestionId() { return questionDao.getNextId(); }
    public static void addQuestion(Question question) { questionDao.save(question); }

    // ---- Results ----
    public static List<Result> loadResults() { return resultDao.findAll(); }
    public static void addResult(Result result) { resultDao.save(result); }
    public static List<Result> loadResultsForUser(String username) { return resultDao.findByUsername(username); }
    public static List<Result> loadResultsForCreator(String creatorUsername) { return resultDao.findByCreator(creatorUsername); }
    public static void updateResultFeedback(String username, int quizId, String date, String feedback) {
        resultDao.updateFeedback(username, quizId, date, feedback);
    }

    // ---- Messages ----
    public static int getNextMessageId() { return messageDao.getNextId(); }
    public static void addMessage(Message message) { messageDao.save(message); }
    public static List<Message> loadConversation(String userA, String userB) { return messageDao.findConversation(userA, userB); }
    public static List<String> loadContactsFor(String username) { return messageDao.findContactsFor(username); }

    // ---- Alerts ----
    public static List<Alert> loadAlerts() { return alertDao.findAll(); }
    public static void addAlert(String message) {
        int newId = 0;
        for (Alert a : alertDao.findAll()) if (a.getId() > newId) newId = a.getId();
        alertDao.save(new Alert(newId + 1, message, now()));
    }

    // ---- Reminders ----
    public static List<Reminder> loadRemindersForUser(String username) { return reminderDao.findByUsername(username); }
    public static int getNextReminderId() { return reminderDao.getNextId(); }
    public static void addReminder(Reminder reminder) { reminderDao.save(reminder); }
    public static void deleteReminder(int reminderId) { reminderDao.delete(reminderId); }

    // ---- Settings ----
    public static Settings loadSettings() { return settingsDao.load(); }
    public static void saveSettings(Settings settings) { settingsDao.save(settings); }
}
