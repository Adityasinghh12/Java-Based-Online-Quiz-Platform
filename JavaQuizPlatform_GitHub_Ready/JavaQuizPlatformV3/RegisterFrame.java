import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * RegisterFrame.java
 * Lets a new user create an account as either a Quiz Creator or a
 * Participant. (The Admin account is not self-registerable — it is
 * created automatically on first run.)
 */
public class RegisterFrame extends JFrame {
    private JTextField usernameField, fullNameField, emailField;
    private JPasswordField passwordField, confirmField;
    private JComboBox<String> roleBox;

    public RegisterFrame(LoginFrame parent) {
        setTitle("Register - New Account");
        setSize(420, 380);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 7, 7, 7);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; add(new JLabel("Full Name:"), gbc);
        fullNameField = new JTextField(16);
        gbc.gridx = 1; gbc.gridy = row++; add(fullNameField, gbc);

        gbc.gridx = 0; gbc.gridy = row; add(new JLabel("Email:"), gbc);
        emailField = new JTextField(16);
        gbc.gridx = 1; gbc.gridy = row++; add(emailField, gbc);

        gbc.gridx = 0; gbc.gridy = row; add(new JLabel("Choose Username:"), gbc);
        usernameField = new JTextField(16);
        gbc.gridx = 1; gbc.gridy = row++; add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = row; add(new JLabel("Choose Password:"), gbc);
        passwordField = new JPasswordField(16);
        gbc.gridx = 1; gbc.gridy = row++; add(passwordField, gbc);

        gbc.gridx = 0; gbc.gridy = row; add(new JLabel("Confirm Password:"), gbc);
        confirmField = new JPasswordField(16);
        gbc.gridx = 1; gbc.gridy = row++; add(confirmField, gbc);

        gbc.gridx = 0; gbc.gridy = row; add(new JLabel("Register As:"), gbc);
        roleBox = new JComboBox<>(new String[]{"Participant", "Quiz Creator"});
        gbc.gridx = 1; gbc.gridy = row++; add(roleBox, gbc);

        JButton registerBtn = new JButton("Register");
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        add(registerBtn, gbc);

        registerBtn.addActionListener(e -> attemptRegister(parent));

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                parent.setVisible(true);
            }
        });
    }

    private void attemptRegister(LoginFrame parent) {
        String fullName = DataStore.sanitize(fullNameField.getText());
        String email = DataStore.sanitize(emailField.getText());
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirm = new String(confirmField.getPassword());
        String roleChoice = (String) roleBox.getSelectedItem();
        String role = roleChoice.equals("Quiz Creator") ? "QUIZ_CREATOR" : "PARTICIPANT";

        if (fullName.isEmpty() || username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields.");
            return;
        }
        if (!password.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.");
            return;
        }
        if (DataStore.userExists(username)) {
            JOptionPane.showMessageDialog(this, "Username already taken.");
            return;
        }

        DataStore.addUser(User.create(role, username, password, fullName, email));
        JOptionPane.showMessageDialog(this, "Registration successful as " + roleChoice + "! Please login.");
        dispose();
        parent.setVisible(true);
    }
}
