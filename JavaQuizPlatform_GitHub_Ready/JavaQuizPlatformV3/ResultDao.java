import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** ResultDao.java — JDBC operations for the `results` table. */
public class ResultDao implements Dao<Result> {

    @Override
    public void save(Result r) throws DataAccessException {
        String sql = "INSERT INTO results (username, quiz_id, quiz_title, score, total_questions, date, feedback) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, r.getUsername());
            ps.setInt(2, r.getQuizId());
            ps.setString(3, r.getQuizTitle());
            ps.setInt(4, r.getScore());
            ps.setInt(5, r.getTotalQuestions());
            ps.setString(6, r.getDate());
            ps.setString(7, r.getFeedback());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to save result.", e);
        }
    }

    @Override
    public List<Result> findAll() throws DataAccessException {
        List<Result> list = new ArrayList<>();
        try (Statement stmt = DBConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM results ORDER BY id")) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new DataAccessException("Failed to load results.", e);
        }
        return list;
    }

    public List<Result> findByUsername(String username) throws DataAccessException {
        List<Result> list = new ArrayList<>();
        String sql = "SELECT * FROM results WHERE username = ? COLLATE NOCASE ORDER BY id";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to load results for user: " + username, e);
        }
        return list;
    }

    /** Results for every quiz created by the given Quiz Creator (a join across quizzes + results). */
    public List<Result> findByCreator(String creatorUsername) throws DataAccessException {
        List<Result> list = new ArrayList<>();
        String sql = "SELECT r.* FROM results r JOIN quizzes q ON r.quiz_id = q.id WHERE q.creator_username = ? COLLATE NOCASE ORDER BY r.id";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, creatorUsername);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to load results for creator: " + creatorUsername, e);
        }
        return list;
    }

    public void updateFeedback(String username, int quizId, String date, String feedback) throws DataAccessException {
        String sql = "UPDATE results SET feedback = ? WHERE username = ? AND quiz_id = ? AND date = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, feedback);
            ps.setString(2, username);
            ps.setInt(3, quizId);
            ps.setString(4, date);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update feedback.", e);
        }
    }

    private Result mapRow(ResultSet rs) throws SQLException {
        return new Result(rs.getString("username"), rs.getInt("quiz_id"), rs.getString("quiz_title"),
                rs.getInt("score"), rs.getInt("total_questions"), rs.getString("date"), rs.getString("feedback"));
    }
}
