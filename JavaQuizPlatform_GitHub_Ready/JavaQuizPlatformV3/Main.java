import javax.swing.*;

/**
 * Main.java
 * Entry point. Initializes the embedded SQLite database (creating the
 * schema and seed data on first run) and then opens the login screen.
 */
public class Main {
    public static void main(String[] args) {
        try {
            DataStore.initializeIfNeeded();
        } catch (DataAccessException e) {
            JOptionPane.showMessageDialog(null,
                    "Could not initialize the database:\n" + e.getMessage(),
                    "Startup Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
