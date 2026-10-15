/**
 * Question.java
 * Simple data model representing one MCQ (Multiple Choice Question).
 * Each question belongs to exactly one Quiz (via quizId) and has 4
 * options (A, B, C, D) with one correct option.
 */
public class Question {
    private int id;
    private int quizId;
    private String questionText;
    private String optionA, optionB, optionC, optionD;
    private char correctOption; // 'A', 'B', 'C', or 'D'

    public Question(int id, int quizId, String questionText, String optionA, String optionB,
                     String optionC, String optionD, char correctOption) {
        this.id = id;
        this.quizId = quizId;
        this.questionText = questionText;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctOption = correctOption;
    }

    public int getId() { return id; }
    public int getQuizId() { return quizId; }
    public String getQuestionText() { return questionText; }
    public String getOptionA() { return optionA; }
    public String getOptionB() { return optionB; }
    public String getOptionC() { return optionC; }
    public String getOptionD() { return optionD; }
    public char getCorrectOption() { return correctOption; }

    /**
     * Converts this question into one line of text so it can be
     * saved into questions.txt. Fields are separated by "|".
     */
    public String toFileString() {
        return id + "|" + quizId + "|" + questionText + "|" + optionA + "|" + optionB + "|" + optionC + "|" + optionD + "|" + correctOption;
    }

    /**
     * Rebuilds a Question object from one line read out of questions.txt.
     */
    public static Question fromFileString(String line) {
        String[] parts = line.split("\\|", -1);
        return new Question(
                Integer.parseInt(parts[0]),
                Integer.parseInt(parts[1]),
                parts[2],
                parts[3],
                parts[4],
                parts[5],
                parts[6],
                parts[7].charAt(0)
        );
    }
}
