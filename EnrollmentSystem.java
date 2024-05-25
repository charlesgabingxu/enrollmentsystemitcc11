import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class EnrollmentSystem {

    private static Connection connection;
    private static int userId;

    public static void main(String[] args) {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost/enrollment", "myUser", "1234");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database connection error: " + e.getMessage());
            System.exit(1);
        }

        SwingUtilities.invokeLater(() -> {
            new LoginWindow().setVisible(true);
        });
    }

    static class LoginWindow extends JFrame {
        private JTextField emailField;
        private JPasswordField passwordField;

        public LoginWindow() {
            setTitle("Login");
            setSize(300, 200);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);

            JPanel panel = new JPanel();
            panel.setLayout(new GridLayout(4, 2));

            panel.add(new JLabel("Email:"));
            emailField = new JTextField();
            panel.add(emailField);

            panel.add(new JLabel("Password:"));
            passwordField = new JPasswordField();
            panel.add(passwordField);

            JButton loginButton = new JButton("Login");
            loginButton.addActionListener(e -> login());
            panel.add(loginButton);

            JButton registerButton = new JButton("Register");
            registerButton.addActionListener(e -> {
                new RegisterWindow().setVisible(true);
                dispose();
            });
            panel.add(registerButton);

            JButton exitButton = new JButton("Exit");
            exitButton.addActionListener(e -> System.exit(0));
            panel.add(exitButton);

            add(panel);
        }

        private void login() {
            String email = emailField.getText();
            String password = new String(passwordField.getPassword());

            try {
                String query = "SELECT * FROM users WHERE email=? AND password=?";
                PreparedStatement stmt = connection.prepareStatement(query);
                stmt.setString(1, email);
                stmt.setString(2, password);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    userId = rs.getInt("id");
                    new MainWindow(userId).setVisible(true);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid email or password.");
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }

    static class RegisterWindow extends JFrame {
        private JTextField emailField;
        private JPasswordField passwordField;
        private JPasswordField confirmPasswordField;

        public RegisterWindow() {
            setTitle("Register");
            setSize(300, 200);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);

            JPanel panel = new JPanel();
            panel.setLayout(new GridLayout(5, 2));

            panel.add(new JLabel("Email:"));
            emailField = new JTextField();
            panel.add(emailField);

            panel.add(new JLabel("Password:"));
            passwordField = new JPasswordField();
            panel.add(passwordField);

            panel.add(new JLabel("Confirm Password:"));
            confirmPasswordField = new JPasswordField();
            panel.add(confirmPasswordField);

            JButton registerButton = new JButton("Register");
            registerButton.addActionListener(e -> register());
            panel.add(registerButton);

            JButton backButton = new JButton("Back");
            backButton.addActionListener(e -> {
                new LoginWindow().setVisible(true);
                dispose();
            });
            panel.add(backButton);

            add(panel);
        }

        private void register() {
            String email = emailField.getText();
            String password = new String(passwordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());

            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match.");
                return;
            }

            try {
                String query = "INSERT INTO users (email, password) VALUES (?, ?)";
                PreparedStatement stmt = connection.prepareStatement(query);
                stmt.setString(1, email);
                stmt.setString(2, password);
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Registration Successful!");
                new LoginWindow().setVisible(true);
                dispose();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }

    static class MainWindow extends JFrame {
        private int userId;

        public MainWindow(int userId) {
            this.userId = userId;
            setTitle("Main Menu");
            setSize(300, 200);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);

            JPanel panel = new JPanel();
            panel.setLayout(new GridLayout(3, 1));

            JButton checkAccountButton = new JButton("Check Account");
            checkAccountButton.addActionListener(e -> checkAccount());
            panel.add(checkAccountButton);

            JButton enrollButton = new JButton("Enroll");
            enrollButton.addActionListener(e -> {
                if (isUserEnrolled()) {
                    JOptionPane.showMessageDialog(this, "You are already enrolled. Cancel your current enrollment first.");
                } else {
                    new EnrollWindow(userId).setVisible(true);
                    dispose();
                }
            });
            panel.add(enrollButton);

            JButton logoutButton = new JButton("Logout");
            logoutButton.addActionListener(e -> {
                new LoginWindow().setVisible(true);
                dispose();
            });
            panel.add(logoutButton);

            add(panel);
        }

        private void checkAccount() {
            try {
                String query = "SELECT * FROM enrollments WHERE user_id=?";
                PreparedStatement stmt = connection.prepareStatement(query);
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    String info = String.format(
                            "Name: %s\nDate of Birth: %s\nAge: %s\nGender: %s\nPhone Number: %s\nAddress: %s\nCourse: %s",
                            rs.getString("name"), rs.getString("date_of_birth"), rs.getString("age"),
                            rs.getString("gender"), rs.getString("phone_number"), rs.getString("address"),
                            rs.getString("course")
                    );

                    int response = JOptionPane.showConfirmDialog(this, info, "Account Details",
                            JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);

                    if (response == JOptionPane.CANCEL_OPTION) {
                        cancelEnrollment();
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "You have not enrolled yet.");
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }

        private void cancelEnrollment() {
            try {
                String query = "DELETE FROM enrollments WHERE user_id=?";
                PreparedStatement stmt = connection.prepareStatement(query);
                stmt.setInt(1, userId);
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Your enrollment has been canceled.");
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }

        private boolean isUserEnrolled() {
            try {
                String query = "SELECT * FROM enrollments WHERE user_id=?";
                PreparedStatement stmt = connection.prepareStatement(query);
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();
                return rs.next();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
                return false;
            }
        }
    }

    static class EnrollWindow extends JFrame {
        private int userId;
        private JTextField nameField;
        private JTextField dobField;
        private JTextField ageField;
        private JComboBox<String> genderField;
        private JTextField phoneField;
        private JTextArea addressField;
        private JComboBox<String> courseField;

        public EnrollWindow(int userId) {
            this.userId = userId;
            setTitle("Enroll");
            setSize(400, 300);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);

            JPanel panel = new JPanel();
            panel.setLayout(new GridLayout(8, 2));

            panel.add(new JLabel("Name:"));
            nameField = new JTextField();
            panel.add(nameField);

            panel.add(new JLabel("Date of Birth (YYYY-MM-DD):"));
            dobField = new JTextField();
            panel.add(dobField);

            panel.add(new JLabel("Age:"));
            ageField = new JTextField();
            panel.add(ageField);

            panel.add(new JLabel("Gender:"));
            genderField = new JComboBox<>(new String[]{"Male", "Female", "Other"});
            panel.add(genderField);

            panel.add(new JLabel("Phone Number:"));
            phoneField = new JTextField();
            panel.add(phoneField);

            panel.add(new JLabel("Address:"));
            addressField = new JTextArea();
            panel.add(new JScrollPane(addressField));

            panel.add(new JLabel("Course:"));
            courseField = new JComboBox<>(new String[]{"Computer Science", "Law", "Medicine", "Business Administration", "Agriculture"});
            panel.add(courseField);

            JButton submitButton = new JButton("Submit");
            submitButton.addActionListener(e -> submitEnrollment());
            panel.add(submitButton);

            JButton backButton = new JButton("Back");
            backButton.addActionListener(e -> {
                new MainWindow(userId).setVisible(true);
                dispose();
            });
            panel.add(backButton);

            add(panel);
        }

        private void submitEnrollment() {
            String name = nameField.getText();
            String dob = dobField.getText();
            String age = ageField.getText();
            String gender = (String) genderField.getSelectedItem();
            String phone = phoneField.getText();
            String address = addressField.getText();
            String course = (String) courseField.getSelectedItem();

            try {
                String query = "INSERT INTO enrollments (user_id, name, date_of_birth, age, gender, phone_number, address, course) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement stmt = connection.prepareStatement(query);
                stmt.setInt(1, userId);
                stmt.setString(2, name);
                stmt.setString(3, dob);
                stmt.setString(4, age);
                stmt.setString(5, gender);
                stmt.setString(6, phone);
                stmt.setString(7, address);
                stmt.setString(8, course);
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Enrollment Successful!");
                new MainWindow(userId).setVisible(true);
                dispose();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }
}
