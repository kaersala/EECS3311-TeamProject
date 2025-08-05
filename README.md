# EECS3311-TeamProject-Group C
Nutrient tracking and food swap system

## Deliverable 1: Implementation & Demo Instructions
This project implements the following five core functionalities as required by Deliverable 1:

1. Profile Creation
2. Meal Logging
3. Food Item Swap
4. JFreeChart Visualization
5. Background Calculation (Nutrient Calculation/Swap Effect)

Each functionality can be demonstrated via a dedicated main method.

## Deliverable 2: Advanced Features & User Experience Enhancement

### Application Features:

#### **NutriSci - Main Menu Functions:**
1. **Goals** - Set and manage nutritional targets (fiber, calories, protein, etc.)
2. **Log Meal** - Record daily meals with ingredients and quantities
3. **View Journal** - Browse meal history and nutrition analysis
4. **Edit Profile** - Manage user information and settings
5. **Switch User** - Change between different user profiles
6. **Exit** - Close the application

#### **Core Features Implemented:**

##### 1. **Enhanced Food Swap System**
- Goal-based food replacement with structured nutritional targets
- Support for up to 2 simultaneous nutritional goals
- Intelligent swap suggestions maintaining nutrient balance
- Before/after nutrient comparison with visual indicators
- Swap application to individual meals with rollback capability

##### 2. **Basic Chart Visualization Framework**
- Daily nutrition breakdown display
- Weekly calorie trend visualization
- Goal progress tracking charts
- Basic CFG compliance display (sample data)
- Extensible chart system with decorator pattern

##### 3. **Advanced User Interface Improvements**
- Unified color scheme for all icon elements
- Contextual swap suggestions within journal view
- Enhanced meal comparison interface
- Improved data presentation and user feedback
- Loading indicators for time-consuming operations

##### 4. **Database Integration & Data Management**
- Full MySQL database support for user profiles and meal data
- User profile persistence with settings management
- Meal and ingredient tracking with nutritional calculations
- Goal management and storage system

### Technical Architecture:

#### **Strategy Pattern Implementation**
- Modular swap strategies for different nutritional goals:
  - CalorieSwapStrategy
  - ProteinSwapStrategy
  - CarbsSwapStrategy
  - FatSwapStrategy
  - FiberSwapStrategy

#### **Service Layer Architecture**
- SwapService: Core swap management and application
- SwapBatchApplier: Batch operations for multiple meals
- CFGComparisonEngine: Basic CFG guideline comparison (framework)
- RecommendationEngine: Food recommendation system

#### **Chart System**
- Extensible chart framework with decorator pattern
- Multiple chart types: BarChart, PieChart, SwingChart
- Chart factories for different data types
- Interactive chart decorators

#### **Controller Architecture**
- UserProfileController: Profile and settings management
- MealLoggerController: Meal logging and journal management
- MealSwappingController: Swap suggestion and application
- DailyIntakeAndCFGAnalysisController: Nutrition analysis

### Demo Instructions:
Each advanced feature can be demonstrated through the main application interface:
- **Main.java**: Complete application with all features integrated
- **SwapApp.java**: Food swap functionality demonstration
- **ProfileApp.java**: Enhanced profile management
- **MealEntryApp.java**: Improved meal logging system

### Database Requirements:
- MySQL database with CNF2015 nutritional data
- User profiles and goals tables
- Meal and ingredient tracking tables
- Swap history and effect analysis tables

### Current Limitations:
- CFG compliance analysis uses sample data (not fully integrated)
- Cross-meal substitution framework exists but may need refinement
- Advanced chart visualizations are basic (text-based displays)
- Some features may require additional testing and refinement

### Future Enhancements:
- Complete CFG integration with real dietary analysis
- Advanced cross-meal substitution with time-based filtering
- Enhanced chart visualizations with interactive features
- Comprehensive nutrient tracking and goal achievement analysis
