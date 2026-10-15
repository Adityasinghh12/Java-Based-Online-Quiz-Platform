import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** QuizDao.java — JDBC operations for the `quizzes` table. */
public class QuizDao implements Dao<Quiz> {

    @Override
    public void save(Quiz quiz) throws DataAccessException {
        String sql = "INSERT INTO quizzes (title, description, time_limit_minutes, creator_username, status) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, quiz.getTitle());
            ps.setString(2, quiz.getDescription());
            ps.setInt(3, quiz.getTimeLimitMinutes());
            ps.setString(4, quiz.getCreatorUsername());
            ps.setString(5, quiz.getStatus());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to save quiz: " + quiz.getTitle(), e);
        }
    }

    @Override
    public List<Quiz> findAll() throws DataAccessException {
        List<Quiz> list = new ArrayList<>();
        String sql = "SELECT * FROM quizzes ORDER BY id";
        try (Statement stmt = DBConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new DataAccessException("Failed to load quizzes.", e);
        }
        return list;
    }

    public Quiz findById(int id) throws DataAccessException {
        String sql = "SELECT * FROM quizzes WHERE id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to look up quiz id: " + id, e);
        }
        return null;
    }

    public List<Quiz> findByCreator(String username) throws DataAccessException {
        List<Quiz> list = new ArrayList<>();
        String sql = "SELECT * FROM quizzes WHERE creator_username = ? COLLATE NOCASE ORDER BY id";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to load quizzes for creator: " + username, e);
        }
        return list;
    }

    public List<Quiz> findByStatus(String status) throws DataAccessException {
        List<Quiz> list = new ArrayList<>();
        String sql = "SELECT * FROM quizzes WHERE status = ? ORDER BY id";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to load quizzes with status: " + status, e);
        }
        return list;
    }

    public synchronized int getNextId() throws DataAccessException {
        // synchronized: guards against two Quiz Creators submitting a
        // quiz at the exact same moment and colliding on the same id.
        String sql = "SELECT MAX(id) AS max_id FROM quizzes";
        try (Statement stmt = DBConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt("max_id") + 1;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to compute next quiz id.", e);
        }
        return 1;
    }

    public void updateStatus(int id, String status) throws DataAccessException {
        String sql = "UPDATE quizzes SET status = ? WHERE id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update status for quiz id: " + id, e);
        }
    }

    public void delete(int id) throws DataAccessException {
        String sql = "DELETE FROM quizzes WHERE id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete quiz id: " + id, e);
        }
    }

    private Quiz mapRow(ResultSet rs) throws SQLException {
        return new Quiz(rs.getInt("id"), rs.getString("title"), rs.getString("description"),
                rs.getInt("time_limit_minutes"), rs.getString("creator_username"), rs.getString("status"));
    }
}
