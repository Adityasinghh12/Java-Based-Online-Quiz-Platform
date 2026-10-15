import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * DBConnection.java
 * Central place that owns the JDBC connection to our embedded SQLite
 * database (quizplatform.db, a single file - no server to install)
 * and creates the schema on first run. All 8 DAO classes obtain their
 * Connection from here rather than opening their own, which keeps
 * connection handling in one place.
 */
public class DBConnection {
    private static final String DB_URL = "jdbc:sqlite:quizplatform.db";
    private static Connection connection;

    /** Returns the single shared JDBC connection, opening it (and
     *  creating the schema) the first time it's requested. */
    public static synchronized Connection getConnection() {
        if (connection == null) {
            try {
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection(DB_URL);
                initializeSchema(connection);
            } catch (ClassNotFoundException | SQLException e) {
                throw new DataAccessException("Could not connect to the database.", e);
            }
        }
        return connection;
    }

    private static void initializeSchema(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "username TEXT PRIMARY KEY, password TEXT NOT NULL, role TEXT NOT NULL, " +
                    "full_name TEXT, email TEXT)");

            stmt.execute("CREATE TABLE IF NOT EXISTS quizzes (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, description TEXT, " +
                    "time_limit_minutes INTEGER, creator_username TEXT, status TEXT)");

            stmt.execute("CREATE TABLE IF NOT EXISTS questions (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, quiz_id INTEGER, question_text TEXT, " +
                    "option_a TEXT, option_b TEXT, option_c TEXT, option_d TEXT, correct_option TEXT)");

            stmt.execute("CREATE TABLE IF NOT EXISTS results (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT, quiz_id INTEGER, " +
                    "quiz_title TEXT, score INTEGER, total_questions INTEGER, date TEXT, feedback TEXT)");

            stmt.execute("CREATE TABLE IF NOT EXISTS messages (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, from_username TEXT, to_username TEXT, " +
                    "quiz_id INTEGER, text TEXT, date TEXT)");

            stmt.execute("CREATE TABLE IF NOT EXISTS alerts (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, message TEXT, date TEXT)");

            stmt.execute("CREATE TABLE IF NOT EXISTS reminders (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT, quiz_id INTEGER, " +
                    "quiz_title TEXT, note TEXT, date TEXT)");

            stmt.execute("CREATE TABLE IF NOT EXISTS settings (" +
                    "id INTEGER PRIMARY KEY CHECK (id = 1), platform_name TEXT, " +
                    "default_time_limit INTEGER, passing_percentage INTEGER)");
        }
    }
}
