/** ParticipantUser.java — concrete User subclass for the Participant role. */
public class ParticipantUser extends User {
    public ParticipantUser(String username, String password, String fullName, String email) {
        super(username, password, fullName, email);
    }

    @Override
    public String getRole() { return "PARTICIPANT"; }

    @Override
    public String getDashboardTitle() { return "Participant Dashboard"; }
}
