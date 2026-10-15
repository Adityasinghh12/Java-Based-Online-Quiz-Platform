/**
 * Quiz.java
 * Represents one quiz set (e.g. "Java Basics"). A Quiz Creator submits
 * a quiz, which starts life as PENDING until an Admin APPROVES or
 * REJECTS it. Only APPROVED quizzes are visible to Participants.
 */
public class Quiz {
    private int id;
    private String title;
    private String description;
    private int timeLimitMinutes;
    private String creatorUsername;
    private String status; // "PENDING", "APPROVED", "REJECTED"

    public Quiz(int id, String title, String description, int timeLimitMinutes,
                String creatorUsername, String status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.timeLimitMinutes = timeLimitMinutes;
        this.creatorUsername = creatorUsername;
        this.status = status;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getTimeLimitMinutes() { return timeLimitMinutes; }
    public String getCreatorUsername() { return creatorUsername; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String toFileString() {
        return id + "|" + title + "|" + description + "|" + timeLimitMinutes + "|" + creatorUsername + "|" + status;
    }

    public static Quiz fromFileString(String line) {
        String[] parts = line.split("\\|", -1);
        return new Quiz(Integer.parseInt(parts[0]), parts[1], parts[2],
                Integer.parseInt(parts[3]), parts[4], parts[5]);
    }

    @Override
    public String toString() {
        return title;
    }
}
