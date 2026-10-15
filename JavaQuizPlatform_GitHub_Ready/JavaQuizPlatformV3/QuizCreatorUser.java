/** QuizCreatorUser.java — concrete User subclass for the Quiz Creator role. */
public class QuizCreatorUser extends User {
    public QuizCreatorUser(String username, String password, String fullName, String email) {
        super(username, password, fullName, email);
    }

    @Override
    public String getRole() { return "QUIZ_CREATOR"; }

    @Override
    public String getDashboardTitle() { return "Quiz Creator Dashboard"; }
}
