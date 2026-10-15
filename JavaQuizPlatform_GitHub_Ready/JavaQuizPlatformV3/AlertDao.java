import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** AlertDao.java — JDBC operations for the `alerts` table. */
public class AlertDao implements Dao<Alert> {

    @Override
    public synchronized void save(Alert alert) throws DataAccessException {
        String sql = "INSERT INTO alerts (message, date) VALUES (?, ?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, alert.getMessage());
            ps.setString(2, alert.getDate());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to save alert.", e);
        }
    }

    @Override
    public List<Alert> findAll() throws DataAccessException {
        List<Alert> list = new ArrayList<>();
        try (Statement stmt = DBConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM alerts ORDER BY id")) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new DataAccessException("Failed to load alerts.", e);
        }
        return list;
    }

    private Alert mapRow(ResultSet rs) throws SQLException {
        return new Alert(rs.getInt("id"), rs.getString("message"), rs.getString("date"));
    }
}
