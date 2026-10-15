/**
 * User.java
 * Abstract base class for every account in the system. Each concrete
 * role (AdminUser, QuizCreatorUser, ParticipantUser) extends this
 * class and supplies its own role identity and dashboard label -
 * a classic Inheritance + Polymorphism design: code elsewhere can
 * hold a plain `User` reference and call getRole()/getDashboardTitle()
 * without caring which concrete subclass it actually is; the correct
 * overridden version runs automatically (dynamic dispatch).
 */
public abstract class User {
    protected String username;
    protected String password;
    protected String fullName;
    protected String email;

    protected User(String username, String password, String fullName, String email) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }

    /** The role identity string stored in the database ("ADMIN", "QUIZ_CREATOR", "PARTICIPANT"). */
    public abstract String getRole();

    /** The window title shown on this role's dashboard - overridden per subclass. */
    public abstract String getDashboardTitle();

    /** Shared helper built on top of the polymorphic getRole(). */
    public String describeAccount() {
        return fullName + " (" + getRole() + ")";
    }

    /**
     * Factory method: given a role string (as stored in the DB) and
     * the account fields, constructs the correct concrete subclass.
     * Centralizing this here means only one place needs to know the
     * mapping from role string -> class.
     */
    public static User create(String role, String username, String password, String fullName, String email) {
        switch (role) {
            case "ADMIN":
                return new AdminUser(username, password, fullName, email);
            case "QUIZ_CREATOR":
                return new QuizCreatorUser(username, password, fullName, email);
            case "PARTICIPANT":
                return new ParticipantUser(username, password, fullName, email);
            default:
                throw new IllegalArgumentException("Unknown role: " + role);
        }
    }
}
