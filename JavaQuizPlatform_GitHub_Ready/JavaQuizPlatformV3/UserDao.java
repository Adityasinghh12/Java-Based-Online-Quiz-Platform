import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * UserDao.java
 * All JDBC operations for the `users` table. Reconstructs the correct
 * polymorphic User subclass (AdminUser / QuizCreatorUser /
 * ParticipantUser) from each row via User.create().
 */
public class UserDao implements Dao<User> {

    @Override
    public void save(User user) throws DataAccessException {
        String sql = "INSERT INTO users (username, password, role, full_name, email) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getRole());
            ps.setString(4, user.getFullName());
            ps.setString(5, user.getEmail());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to save user: " + user.getUsername(), e);
        }
    }

    @Override
    public List<User> findAll() throws DataAccessException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Statement stmt = DBConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new DataAccessException("Failed to load users.", e);
        }
        return list;
    }

    public User findByUsername(String username) throws DataAccessException {
        String sql = "SELECT * FROM users WHERE username = ? COLLATE NOCASE";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to look up user: " + username, e);
        }
        return null;
    }

    /** Throws AuthenticationException on bad credentials instead of returning null -
     *  a login failure is treated as an exceptional, must-handle condition. */
    public User authenticate(String username, String password) throws AuthenticationException {
        User user = findByUsername(username);
        if (user == null || !user.getPassword().equals(password)) {
            throw new AuthenticationException("Invalid username or password.");
        }
        return user;
    }

    public boolean exists(String username) throws DataAccessException {
        return findByUsername(username) != null;
    }

    public void updateRole(String username, String newRole) throws DataAccessException {
        String sql = "UPDATE users SET role = ? WHERE username = ? COLLATE NOCASE";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, newRole);
            ps.setString(2, username);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update role for: " + username, e);
        }
    }

    public void delete(String username) throws DataAccessException {
        String sql = "DELETE FROM users WHERE username = ? COLLATE NOCASE";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, username);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete user: " + username, e);
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        return User.create(rs.getString("role"), rs.getString("username"), rs.getString("password"),
                rs.getString("full_name"), rs.getString("email"));
    }
}
