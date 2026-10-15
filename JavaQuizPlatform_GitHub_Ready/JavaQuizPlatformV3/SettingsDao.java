import java.sql.*;

/**
 * SettingsDao.java — JDBC operations for the single-row `settings`
 * table. Does not implement Dao<T> since settings is a single record,
 * not a collection - load()/save() express that more honestly than
 * a findAll() that always returns exactly one item.
 */
public class SettingsDao {

    public Settings load() throws DataAccessException {
        String sql = "SELECT * FROM settings WHERE id = 1";
        try (Statement stmt = DBConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return new Settings(rs.getString("platform_name"), rs.getInt("default_time_limit"), rs.getInt("passing_percentage"));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to load settings.", e);
        }
        // No row yet: seed defaults.
        Settings defaults = new Settings("Java Quiz Platform", 5, 50);
        save(defaults);
        return defaults;
    }

    public void save(Settings settings) throws DataAccessException {
        String sql = "INSERT INTO settings (id, platform_name, default_time_limit, passing_percentage) VALUES (1, ?, ?, ?) " +
                "ON CONFLICT(id) DO UPDATE SET platform_name = excluded.platform_name, " +
                "default_time_limit = excluded.default_time_limit, passing_percentage = excluded.passing_percentage";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, settings.getPlatformName());
            ps.setInt(2, settings.getDefaultTimeLimitMinutes());
            ps.setInt(3, settings.getPassingPercentage());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to save settings.", e);
        }
    }
}
