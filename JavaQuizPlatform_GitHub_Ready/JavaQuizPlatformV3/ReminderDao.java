import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** ReminderDao.java — JDBC operations for the `reminders` table. */
public class ReminderDao implements Dao<Reminder> {

    @Override
    public void save(Reminder r) throws DataAccessException {
        String sql = "INSERT INTO reminders (username, quiz_id, quiz_title, note, date) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, r.getUsername());
            ps.setInt(2, r.getQuizId());
            ps.setString(3, r.getQuizTitle());
            ps.setString(4, r.getNote());
            ps.setString(5, r.getDate());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to save reminder.", e);
        }
    }

    @Override
    public List<Reminder> findAll() throws DataAccessException {
        List<Reminder> list = new ArrayList<>();
        try (Statement stmt = DBConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM reminders ORDER BY id")) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new DataAccessException("Failed to load reminders.", e);
        }
        return list;
    }

    public List<Reminder> findByUsername(String username) throws DataAccessException {
        List<Reminder> list = new ArrayList<>();
        String sql = "SELECT * FROM reminders WHERE username = ? COLLATE NOCASE ORDER BY id";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to load reminders for: " + username, e);
        }
        return list;
    }

    public synchronized int getNextId() throws DataAccessException {
        try (Statement stmt = DBConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT MAX(id) AS max_id FROM reminders")) {
            if (rs.next()) return rs.getInt("max_id") + 1;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to compute next reminder id.", e);
        }
        return 1;
    }

    public void delete(int id) throws DataAccessException {
        String sql = "DELETE FROM reminders WHERE id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete reminder id: " + id, e);
        }
    }

    private Reminder mapRow(ResultSet rs) throws SQLException {
        return new Reminder(rs.getInt("id"), rs.getString("username"), rs.getInt("quiz_id"),
                rs.getString("quiz_title"), rs.getString("note"), rs.getString("date"));
    }
}
