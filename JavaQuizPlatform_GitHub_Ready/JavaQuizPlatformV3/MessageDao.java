import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/** MessageDao.java — JDBC operations for the `messages` table. */
public class MessageDao implements Dao<Message> {

    @Override
    public void save(Message m) throws DataAccessException {
        String sql = "INSERT INTO messages (from_username, to_username, quiz_id, text, date) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, m.getFromUsername());
            ps.setString(2, m.getToUsername());
            ps.setInt(3, m.getQuizId());
            ps.setString(4, m.getText());
            ps.setString(5, m.getDate());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to save message.", e);
        }
    }

    @Override
    public List<Message> findAll() throws DataAccessException {
        List<Message> list = new ArrayList<>();
        try (Statement stmt = DBConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM messages ORDER BY id")) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new DataAccessException("Failed to load messages.", e);
        }
        return list;
    }

    public List<Message> findConversation(String userA, String userB) throws DataAccessException {
        List<Message> list = new ArrayList<>();
        String sql = "SELECT * FROM messages WHERE " +
                "(from_username = ? COLLATE NOCASE AND to_username = ? COLLATE NOCASE) OR " +
                "(from_username = ? COLLATE NOCASE AND to_username = ? COLLATE NOCASE) ORDER BY id";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, userA); ps.setString(2, userB);
            ps.setString(3, userB); ps.setString(4, userA);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to load conversation.", e);
        }
        return list;
    }

    public List<String> findContactsFor(String username) throws DataAccessException {
        LinkedHashSet<String> contacts = new LinkedHashSet<>();
        String sql = "SELECT from_username, to_username FROM messages WHERE from_username = ? COLLATE NOCASE OR to_username = ? COLLATE NOCASE ORDER BY id";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String from = rs.getString("from_username");
                    String to = rs.getString("to_username");
                    contacts.add(from.equalsIgnoreCase(username) ? to : from);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to load contacts for: " + username, e);
        }
        return new ArrayList<>(contacts);
    }

    public synchronized int getNextId() throws DataAccessException {
        try (Statement stmt = DBConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT MAX(id) AS max_id FROM messages")) {
            if (rs.next()) return rs.getInt("max_id") + 1;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to compute next message id.", e);
        }
        return 1;
    }

    private Message mapRow(ResultSet rs) throws SQLException {
        return new Message(rs.getInt("id"), rs.getString("from_username"), rs.getString("to_username"),
                rs.getInt("quiz_id"), rs.getString("text"), rs.getString("date"));
    }
}
