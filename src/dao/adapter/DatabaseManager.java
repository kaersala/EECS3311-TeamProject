package dao.adapter;

public class DatabaseManager {
    private static DatabaseManager instance;
    private static DatabaseAdapter adapter;
    
    private DatabaseManager() {
        adapter = new MySQLAdapter();
        adapter.connect();
    }
    
    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    
    public static DatabaseAdapter getAdapter() {
        if (adapter == null) {
            getInstance();
        }
        return adapter;
    }
} 