/** AdminUser.java — concrete User subclass for the Admin role. */
public class AdminUser extends User {
    public AdminUser(String username, String password, String fullName, String email) {
        super(username, password, fullName, email);
    }

    @Override
    public String getRole() { return "ADMIN"; }

    @Override
    public String getDashboardTitle() { return "Admin Dashboard"; }
}
