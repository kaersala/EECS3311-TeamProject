-- Create user_goals table with correct structure
DROP TABLE IF EXISTS user_goals;

CREATE TABLE user_goals (
    GoalID INT AUTO_INCREMENT PRIMARY KEY,
    UserID INT NOT NULL,
    Nutrient VARCHAR(50) NOT NULL,
    Direction VARCHAR(20) NOT NULL,
    Amount DOUBLE NOT NULL,
    Intensity VARCHAR(20) NOT NULL,
    CreatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (UserID) REFERENCES user_profile(UserID) ON DELETE CASCADE
);

-- Insert some sample data
INSERT INTO user_goals (UserID, Nutrient, Direction, Amount, Intensity) VALUES
(1, 'Fiber', 'Increase', 5.0, 'moderate'),
(1, 'Calories', 'Decrease', 200.0, 'low'),
(2, 'Protein', 'Increase', 10.0, 'high'),
(2, 'Sodium', 'Decrease', 500.0, 'moderate');

-- Show the table structure
DESCRIBE user_goals;

-- Show sample data
SELECT * FROM user_goals; 