import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    // Database URL
    private static final String URL = "jdbc:mysql://localhost:3306/studentdb";

    // Database credentials
    private static final String USERNAME = "root"; // Replace with your MySQL username
    private static final String PASSWORD = "";     // Replace with your MySQL password

    /**
     * Reusable method — call this from any DAO/GUI class whenever you need a connection.
     * Example: try (Connection conn = DatabaseConnection.getConnection()) { ... }
     */
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found.", e);
        }
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    // Kept so you can still run this file directly to sanity-check the connection
    public static void main(String[] args) {
        try (Connection connection = getConnection()) {
            if (connection != null) {
                System.out.println("Connected to the database successfully!");
            }
        } catch (SQLException e) {
            System.out.println("Connection failed! Check output console.");
            e.printStackTrace();
        }
    }
}