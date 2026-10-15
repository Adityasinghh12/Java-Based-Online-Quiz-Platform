import javax.swing.*;
import java.awt.*;

/**
 * LoginFrame.java
 * First screen the user sees. All three roles (Admin, Quiz Creator,
 * Participant) log in here; the role stored against their username
 * decides which dashboard opens.
 */
public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginFrame() {
        setTitle("Java Quiz Platform - Login");
        setSize(420, 320);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("Java Quiz Platform", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1;
        add(new JLabel("Username:"), gbc);
        usernameField = new JTextField(15);
        gbc.gridx = 1;
        add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        add(new JLabel("Password:"), gbc);
        passwordField = new JPasswordField(15);
        gbc.gridx = 1;
        add(passwordField, gbc);

        JButton loginBtn = new JButton("Login");
        JButton registerBtn = new JButton("New User? Register");

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        add(loginBtn, gbc);
        gbc.gridy = 4;
        add(registerBtn, gbc);

        JLabel hintLabel = new JLabel(
                "<html><i>Admin: admin / admin123<br>Sample Quiz Creator: creator1 / creator123</i></html>",
                SwingConstants.CENTER);
        gbc.gridy = 5;
        add(hintLabel, gbc);

        passwordField.addActionListener(e -> handleLogin());
        loginBtn.addActionListener(e -> handleLogin());
        registerBtn.addActionListener(e -> {
            setVisible(false);
            new RegisterFrame(this).setVisible(true);
        });
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password.");
            return;
        }

        // authenticate() throws a checked AuthenticationException on bad
        // credentials instead of returning null - the compiler forces us
        // to handle that case explicitly, right here.
        User user;
        try {
            user = DataStore.authenticate(username, password);
        } catch (AuthenticationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Login Failed", JOptionPane.ERROR_MESSAGE);
            passwordField.setText("");
            return;
        } catch (DataAccessException ex) {
            JOptionPane.showMessageDialog(this, "Could not reach the database: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        dispose();
        // Polymorphic dispatch: each concrete User subclass opens its own
        // dashboard; getDashboardTitle() below also comes from the same
        // overridden-per-subclass mechanism.
        if (user instanceof AdminUser) {
            new AdminDashboard().setVisible(true);
        } else if (user instanceof QuizCreatorUser) {
            new QuizCreatorDashboard(user).setVisible(true);
        } else {
            new ParticipantDashboard(user).setVisible(true);
        }
    }
}
