package dao.interfaces;

import model.Goal;
import java.util.List;

/**
 * Data Access Object interface for Goal entities
 */
public interface IGoalDAO {
    
    /**
     * Save goals for a specific user
     * @param userId the user ID
     * @param goals list of goals to save
     */
    void saveGoals(int userId, List<Goal> goals);
    
    /**
     * Load goals for a specific user
     * @param userId the user ID
     * @return list of user's goals
     */
    List<Goal> loadGoals(int userId);
    
    /**
     * Update goals for a specific user
     * @param userId the user ID
     * @param goals updated list of goals
     */
    void updateGoals(int userId, List<Goal> goals);
    
    /**
     * Delete all goals for a specific user
     * @param userId the user ID
     */
    void deleteGoals(int userId);
    
    /**
     * Check if user has any goals set
     * @param userId the user ID
     * @return true if user has goals, false otherwise
     */
    boolean hasGoals(int userId);
} 