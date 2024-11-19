package org.billing;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class database {
    public static void main(String[] args) {
        // MySQL connection details
        String jdbcUrl = "jdbc:mysql://localhost:3306/billingsystem"; // Replace with your actual database name
        String username = "root"; // Replace with your MySQL username
        String password = ""; // Replace with your MySQL password

        Connection connection = null;

        try {
            // Establishing a connection to the MySQL database
            connection = DriverManager.getConnection(jdbcUrl, username, password);

            // Optional: Execute a simple query to check the connection
            Statement statement = connection.createStatement();
            statement.executeQuery("SELECT 1");

            System.out.println("Successfully connected to MySQL!");

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Failed to connect to MySQL.");
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
