/**
 * Message.java
 * One message exchanged between a Participant and a Quiz Creator
 * (either direction). Optionally tied to a specific quiz for context.
 */
public class Message {
    private int id;
    private String fromUsername;
    private String toUsername;
    private int quizId; // -1 if not tied to a specific quiz
    private String text;
    private String date;

    public Message(int id, String fromUsername, String toUsername, int quizId, String text, String date) {
        this.id = id;
        this.fromUsername = fromUsername;
        this.toUsername = toUsername;
        this.quizId = quizId;
        this.text = text;
        this.date = date;
    }

    public int getId() { return id; }
    public String getFromUsername() { return fromUsername; }
    public String getToUsername() { return toUsername; }
    public int getQuizId() { return quizId; }
    public String getText() { return text; }
    public String getDate() { return date; }

    public String toFileString() {
        return id + "|" + fromUsername + "|" + toUsername + "|" + quizId + "|" + text + "|" + date;
    }

    public static Message fromFileString(String line) {
        String[] parts = line.split("\\|", -1);
        return new Message(Integer.parseInt(parts[0]), parts[1], parts[2],
                Integer.parseInt(parts[3]), parts[4], parts[5]);
    }
}
