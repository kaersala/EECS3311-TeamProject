package app;

import dao.adapter.MySQLAdapter;
import java.sql.*;

public class CheckTableStructure {
    public static void main(String[] args) {
        MySQLAdapter databaseAdapter = new MySQLAdapter();
        
        try {
            Connection conn = databaseAdapter.connect();
            if (conn != null) {
                System.out.println("=== Checking user_goals table structure ===");
                
                // Check if table exists
                DatabaseMetaData metaData = conn.getMetaData();
                ResultSet tables = metaData.getTables(null, null, "user_goals", null);
                
                if (tables.next()) {
                    System.out.println("✓ user_goals table exists");
                    
                    // Get column information
                    ResultSet columns = metaData.getColumns(null, null, "user_goals", null);
                    System.out.println("\nColumns in user_goals table:");
                    while (columns.next()) {
                        String columnName = columns.getString("COLUMN_NAME");
                        String dataType = columns.getString("TYPE_NAME");
                        System.out.println("  - " + columnName + " (" + dataType + ")");
                    }
                    
                    // Try to select from the table
                    System.out.println("\n=== Sample data from user_goals ===");
                    try (Statement stmt = conn.createStatement()) {
                        ResultSet rs = stmt.executeQuery("SELECT * FROM user_goals LIMIT 5");
                        ResultSetMetaData rsMetaData = rs.getMetaData();
                        int columnCount = rsMetaData.getColumnCount();
                        
                        System.out.println("Columns in result set:");
                        for (int i = 1; i <= columnCount; i++) {
                            System.out.println("  " + i + ". " + rsMetaData.getColumnName(i));
                        }
                        
                        System.out.println("\nData:");
                        while (rs.next()) {
                            for (int i = 1; i <= columnCount; i++) {
                                System.out.print(rs.getString(i) + " | ");
                            }
                            System.out.println();
                        }
                    }
                } else {
                    System.out.println("✗ user_goals table does not exist");
                    
                    // List all tables
                    System.out.println("\nAll tables in database:");
                    ResultSet allTables = metaData.getTables(null, null, "%", null);
                    while (allTables.next()) {
                        String tableName = allTables.getString("TABLE_NAME");
                        System.out.println("  - " + tableName);
                    }
                }
                
                conn.close();
            } else {
                System.out.println("Failed to connect to database");
            }
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 