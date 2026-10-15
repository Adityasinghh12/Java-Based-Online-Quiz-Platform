/**
 * Reminder.java
 * A simple self-set reminder note a Participant keeps about an
 * upcoming or pending quiz.
 */
public class Reminder {
    private int id;
    private String username;
    private int quizId;
    private String quizTitle;
    private String note;
    private String date;

    public Reminder(int id, String username, int quizId, String quizTitle, String note, String date) {
        this.id = id;
        this.username = username;
        this.quizId = quizId;
        this.quizTitle = quizTitle;
        this.note = note;
        this.date = date;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public int getQuizId() { return quizId; }
    public String getQuizTitle() { return quizTitle; }
    public String getNote() { return note; }
    public String getDate() { return date; }

    public String toFileString() {
        return id + "|" + username + "|" + quizId + "|" + quizTitle + "|" + note + "|" + date;
    }

    public static Reminder fromFileString(String line) {
        String[] parts = line.split("\\|", -1);
        return new Reminder(Integer.parseInt(parts[0]), parts[1], Integer.parseInt(parts[2]), parts[3], parts[4], parts[5]);
    }
}
