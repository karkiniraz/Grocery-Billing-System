package org.billing;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class login extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;

    // MySQL connection details
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/billingsystem"; // Replace with your actual database name
    private static final String USERNAME = "root"; // Replace with your MySQL username
    private static final String PASSWORD = ""; // Replace with your MySQL password

    public login() {
        initialize();
    }

    private void initialize() {
        setTitle("Login Page");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());
        Color lightOrange = new Color(99, 102, 241);
        getContentPane().setBackground(lightOrange);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Add padding between components
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("Serif", Font.BOLD, 22));
        usernameLabel.setForeground(Color.black);
        add(usernameLabel, gbc);

        gbc.gridx = 1;
        usernameField = new JTextField(20);
        usernameField.setBackground(new Color(199, 210, 254));
        usernameField.setPreferredSize(new Dimension(300, 25));
        add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Serif", Font.BOLD, 22));
        passwordLabel.setForeground(Color.black);
        add(passwordLabel, gbc);

        gbc.gridx = 1;
        passwordField = new JPasswordField(20);
        passwordField.setPreferredSize(new Dimension(300, 25));
        passwordField.setBackground(new Color(199, 210, 254));
        add(passwordField, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        loginButton = new JButton("Login");
        loginButton.setFont(new Font("Serif", 10, 18));
        loginButton.setBackground(new Color(165, 180, 252));
        add(loginButton, gbc);

        // Add action listener to login button
        loginButton.addActionListener(e -> onLogin());
    }

    private void onLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        // Validate credentials against MySQL
        boolean authenticated = verifyLogin(username, password);

        if (authenticated) {
            SwingUtilities.invokeLater(() -> {
                Dashboard dashboard = new Dashboard();
                dashboard.setVisible(true);
                dispose(); // Close the login window
            });
        } else {
            JOptionPane.showMessageDialog(this, "Invalid username or password. Please try again.");
            // Clear fields or handle invalid login state
            usernameField.setText("");
            passwordField.setText("");
        }
    }

    private boolean verifyLogin(String username, String password) {
        // Implementation using MySQL
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String query = "SELECT * FROM users WHERE username = ? AND password = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);

            ResultSet resultSet = preparedStatement.executeQuery();

            return resultSet.next(); // Return true if a matching record is found
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            login loginPage = new login();
            loginPage.setVisible(true);
        });
    }
}
