import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** QuestionDao.java — JDBC operations for the `questions` table. */
public class QuestionDao implements Dao<Question> {

    @Override
    public void save(Question q) throws DataAccessException {
        String sql = "INSERT INTO questions (quiz_id, question_text, option_a, option_b, option_c, option_d, correct_option) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, q.getQuizId());
            ps.setString(2, q.getQuestionText());
            ps.setString(3, q.getOptionA());
            ps.setString(4, q.getOptionB());
            ps.setString(5, q.getOptionC());
            ps.setString(6, q.getOptionD());
            ps.setString(7, String.valueOf(q.getCorrectOption()));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to save question.", e);
        }
    }

    @Override
    public List<Question> findAll() throws DataAccessException {
        List<Question> list = new ArrayList<>();
        try (Statement stmt = DBConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM questions ORDER BY id")) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new DataAccessException("Failed to load questions.", e);
        }
        return list;
    }

    public List<Question> findByQuizId(int quizId) throws DataAccessException {
        List<Question> list = new ArrayList<>();
        String sql = "SELECT * FROM questions WHERE quiz_id = ? ORDER BY id";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to load questions for quiz id: " + quizId, e);
        }
        return list;
    }

    public synchronized int getNextId() throws DataAccessException {
        try (Statement stmt = DBConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT MAX(id) AS max_id FROM questions")) {
            if (rs.next()) return rs.getInt("max_id") + 1;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to compute next question id.", e);
        }
        return 1;
    }

    public void delete(int id) throws DataAccessException {
        String sql = "DELETE FROM questions WHERE id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete question id: " + id, e);
        }
    }

    public void deleteByQuizId(int quizId) throws DataAccessException {
        String sql = "DELETE FROM questions WHERE quiz_id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, quizId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete questions for quiz id: " + quizId, e);
        }
    }

    private Question mapRow(ResultSet rs) throws SQLException {
        return new Question(rs.getInt("id"), rs.getInt("quiz_id"), rs.getString("question_text"),
                rs.getString("option_a"), rs.getString("option_b"), rs.getString("option_c"),
                rs.getString("option_d"), rs.getString("correct_option").charAt(0));
    }
}
