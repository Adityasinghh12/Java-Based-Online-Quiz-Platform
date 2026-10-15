/**
 * Result.java
 * Represents one quiz attempt by a Participant - which quiz, what they
 * scored, out of how many questions, when, and an optional feedback
 * comment left by the Quiz Creator after reviewing it.
 */
public class Result {
    private String username;
    private int quizId;
    private String quizTitle;
    private int score;
    private int totalQuestions;
    private String date;
    private String feedback;

    public Result(String username, int quizId, String quizTitle, int score,
                  int totalQuestions, String date, String feedback) {
        this.username = username;
        this.quizId = quizId;
        this.quizTitle = quizTitle;
        this.score = score;
        this.totalQuestions = totalQuestions;
        this.date = date;
        this.feedback = feedback == null ? "" : feedback;
    }

    public String getUsername() { return username; }
    public int getQuizId() { return quizId; }
    public String getQuizTitle() { return quizTitle; }
    public int getScore() { return score; }
    public int getTotalQuestions() { return totalQuestions; }
    public String getDate() { return date; }
    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }

    public int getPercentage() {
        return totalQuestions == 0 ? 0 : Math.round((score * 100f) / totalQuestions);
    }

    public String toFileString() {
        return username + "|" + quizId + "|" + quizTitle + "|" + score + "|" + totalQuestions + "|" + date + "|" + feedback;
    }

    public static Result fromFileString(String line) {
        String[] parts = line.split("\\|", -1);
        return new Result(parts[0], Integer.parseInt(parts[1]), parts[2],
                Integer.parseInt(parts[3]), Integer.parseInt(parts[4]), parts[5],
                parts.length > 6 ? parts[6] : "");
    }
}
