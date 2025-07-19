package dao.adapter;

/**
 * Database Configuration Class
 * Used to manage database connection information
 */
public class DatabaseConfig {
    
    // Database connection configuration
    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_PORT = "3306";
    private static final String DEFAULT_DATABASE = "cnf2015";
    private static final String DEFAULT_USERNAME = "root";
    private static final String DEFAULT_PASSWORD = "password";
    
    // Get database URL
    // This method was found through internet research for MySQL JDBC connection
    public static String getDatabaseUrl() {
        return String.format("jdbc:mysql://%s:%s/%s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
                DEFAULT_HOST, DEFAULT_PORT, DEFAULT_DATABASE);
    }
    
    // Get username
    public static String getUsername() {
        return DEFAULT_USERNAME;
    }
    
    // Get password
    public static String getPassword() {
        return DEFAULT_PASSWORD;
    }
    
    // Print configuration information (for debugging)
    public static void printConfig() {
        System.out.println("=== Database Configuration Information ===");
        System.out.println("URL: " + getDatabaseUrl());
        System.out.println("Username: " + getUsername());
        System.out.println("Password: " + getPassword());
        System.out.println("==========================================");
    }
} 
