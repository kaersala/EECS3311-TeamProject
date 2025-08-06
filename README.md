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

## Deliverable 3: Code Quality Analysis & Refactoring

### Code Quality Assessment:
The project underwent comprehensive code quality analysis using IntelliJ IDEA MetricsTree plugin, identifying and addressing 10 representative code smells through systematic refactoring.

### Ten Code Smells Identified and Fixed:

#### **Code Smell 1: Long Method - SwapEngine.findBestSwapForGoal()**
- **Problem**: Method was ~50 lines with high cognitive complexity
- **Solution**: Extracted into `findCandidatesForIngredient()` and `evaluateCandidate()` methods
- **Improvement**: Reduced complexity and improved readability

#### **Code Smell 2: Long Method - SwapEngine.calculateSwapScore()**
- **Problem**: Method was ~35 lines with complex scoring logic
- **Solution**: Decomposed into `calculateBaseScore()`, `calculateTargetBonus()`, `calculateGroupBonus()`
- **Improvement**: Each method now has single responsibility

#### **Code Smell 3: Long Method - SwapEngine.isNutrientBalanced()**
- **Problem**: Method was ~25 lines handling multiple nutrient calculations
- **Solution**: Extracted `calculateNutrientChange()` method
- **Improvement**: Simplified main method logic

#### **Code Smell 4: Feature Envy - SwapEngine.getNutrientValue()**
- **Problem**: Method in SwapEngine accessing FoodItem data extensively
- **Solution**: Moved method to FoodItem class where the data belongs
- **Improvement**: Better encapsulation and data locality

#### **Code Smell 5: Feature Envy - SwapEngine.calculateMealNutrients()**
- **Problem**: Method in SwapEngine performing calculations on FoodItem collections
- **Solution**: Extracted to dedicated NutrientCalculator service class
- **Improvement**: Improved separation of concerns

#### **Code Smell 6: Data Clump - Repeated Goal Parameters**
- **Problem**: Multiple methods had (FoodItem, FoodItem, Goal) parameters with repeated goal data extraction
- **Solution**: Created GoalContext class to encapsulate goal-related data
- **Improvement**: Reduced parameter repetition and improved maintainability

#### **Code Smell 7: Switch Statement - SwapEngine.selectStrategy()**
- **Problem**: Switch statement for creating RecommendationStrategy based on nutrient type
- **Solution**: Replaced with StrategyFactory pattern
- **Improvement**: Better extensibility and adherence to Open/Closed Principle

#### **Code Smell 8: Switch Statement - SwapEngine.getIntensityFactor()**
- **Problem**: Switch statement for intensity factor calculation
- **Solution**: Replaced with IntensityLevel enum
- **Improvement**: Type safety and easier maintenance

#### **Code Smell 9: Code Duplication - Chart Decorators**
- **Problem**: Multiple decorator classes had similar render() method implementations
- **Solution**: Implemented Template Method Pattern with AbstractChartDecorator
- **Improvement**: Eliminated code duplication and simplified decorator implementations

#### **Code Smell 10: Code Duplication - Chart Components**
- **Problem**: BarChart and PieChart had similar structure and common methods
- **Solution**: Applied Template Method Pattern with AbstractChart base class
- **Improvement**: Reduced duplication and improved chart system architecture

### New Classes Created:
- **StrategyFactory**: Factory pattern for strategy creation
- **IntensityLevel**: Enum for intensity level management
- **GoalContext**: Data clump encapsulation
- **NutrientCalculator**: Dedicated nutrient calculation service
- **SwapReasonGenerator**: Specialized reason generation service
- **AbstractChart**: Template method base class for charts
- **AbstractChartDecorator**: Template method base class for decorators

### Code Quality Metrics Improvement:
- **SwapEngine Class**: CLOC reduced from 176 to 121 (-31%), CCC from 27 to 21 (-22%)
- **Overall Complexity**: Significant reduction in cognitive complexity across refactored methods
- **Maintainability**: Improved code structure and responsibility separation
- **Extensibility**: Better support for future enhancements through design patterns

### Applied Design Patterns:
- **Factory Pattern**: StrategyFactory for strategy creation
- **Template Method Pattern**: AbstractChart and AbstractChartDecorator
- **Strategy Pattern**: Enhanced with factory creation
- **Extract Method**: Multiple long methods decomposed
- **Move Method**: Feature envy methods relocated to appropriate classes

### Refactoring Techniques Used:
1. **Extract Method**: Breaking down complex methods into smaller, focused ones
2. **Move Method**: Relocating methods to classes they primarily use
3. **Extract Class**: Creating new classes to encapsulate related data and behavior
4. **Replace with Polymorphism**: Using enums and factories instead of switch statements
5. **Template Method Pattern**: Eliminating code duplication through inheritance

### Quality Improvement Summary:
- **Success Rate**: 100% (10/10 code smells successfully addressed)
- **Code Maintainability**: Substantially improved through better structure
- **Complexity Reduction**: Significant decrease in method and class complexity
- **Architectural Enhancement**: Better separation of concerns and responsibility distribution
