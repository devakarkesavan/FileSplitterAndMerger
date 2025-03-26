package utils;
import java.sql.*;

public class DBConnection {
    private static final String URL = "jdbc:postgresql://localhost:5432/File";
    private static final String USER = "postgres";
    private static final String PASSWORD = "34421227";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("PostgreSQL Driver not found!", e);
        }
    }
}
