# Use Cases - Nutritional Management Application

## Use Case 1: User Profile Management
As a user I want to be able to create a profile in the application.
The profile should contain basic and necessary information about the user such as sex, date of birth, height, weight. It should also contain some basic settings for the application, such as units of measurement (metric vs imperial). When the application starts, I should be able to choose my profile from the splash screen. From the main UI, I should have the option to edit my profile or my settings. 

**ATTENTION**: As your data grows, the user logs may become extensive, and it will be inefficient to store profile data in files. Consider adding this information to the database. 

**ATTENTION2**: If I edit profile data or settings, while other data is present in the UI, this data should be updated accordingly. Consider using loading bars if this update takes time.

## Use Case 2: Diet Data Logging
As a user I want to be able to log my diet data in the application.
I should be presented with a structured, but flexible UI to enter my data. I need to provide the date of the meal, whether it was breakfast, lunch, dinner, or snack. I can have as many snacks I want, but only one of each of the other meals. I need to be able to provide basic ingredients (e.g., tomatoes, bread, eggs, beef etc.) and quantities (approximate) of cooked ingredients per meal. The application should then extract the nutrient information from the database and calculate the meal's nutritional value in terms of calories, proteins, carbs, vitamins, and others. In the journal view, only calories per meal need to appear, but I should be able to view the breakdown of all nutrients if I select a meal.

## Use Case 3: Food Item Replacement for Nutritional Goals
As a user I want to ask the application to make food item replacements to achieve certain nutritional goals.
I should be able to able to state my goals in a structured manner, i.e., not as a text, but making choices out of options provided by the application and completing minimum data. For example, I can ask to increase my fiber intake or reduce my calory intake. I should be allowed to express my goal in terms of specific nutrients and not foods or food categories (e.g., "I want to increase dairy consumption). I should be able to define the "intensity" of the goal, either in an arbitrary manner (e.g., "more than normal" or "by a significant amount") or in a more precise manner (e.g., "increase fiber by at least 2g" or "decrease calory intake by at least 10%"). I should be allowed to state 1 or 2 goals to be achieved together, but for simplicity, no more than 2.

Next, the application should query the database to make reasonable swaps. Primarily, it should try to achieve the goals by keeping all other nutrients constant. For example, if the goal refers to fiber, calories and other nutrients should remain relatively close to the original (e.g., within 5% or 10% of the original value). Secondarily, the application should try to keep the replacement from the same food group. Finally, the application should not try to replace more than one or two food items from the same meal.

## Use Case 4: Before/After Nutrient Comparison
As a user I want to be able to compare my nutrient intake before and after the swaps.
I should be able to see the two meals side by side with respect to their items, with the replacements being highlighted. Tooltips can inform the user how each item changed the nutrient intake. In a different view, I should be able to see all the nutrients that have changed side by side before and after the replacement(s). Different visual indicators should be used to highlight the changes (e.g., colors, arrows etc.).

## Use Case 5: Cross-Meal Substitution Application
As a user I want to be able to apply substitutions across different meals throughout time and observe the cumulative effect.
When I get a swap I like, I can choose to apply it on previously recorded meals. I can choose a specific period in time or to apply it on every meal recorded. I should then be able to see the changes applied over time, either per meal, cumulatively or on average.

## Use Case 6: Daily Nutrient Intake Visualization
As a user I want to be able to visualize my daily nutrient intake.
In this case, I want to select a time period again, but this time I want to know the average daily portions of specific nutrients I took over this period. What percentage was proteins? What percentage was carbohydrates? I want to see the top 5 or 10 visualized and the rest labelled as "Other nutrients". I want to see a notification or a similar visualization to inform how close I am to the recommended daily portions.

## Use Case 7: Canada Food Guide Alignment
As a user I want to know how well my diet aligns with the Canada Food Guide.
CFG2007: https://publications.gc.ca/collections/Collection/H164-38-1-2007E.pdf 
CFG2019: https://food-guide.canada.ca/en/ 
The Canada Food Guide recommends specific portions from specific food groups. The CFG from 2007 is more detailed, but it was revised in 2019 to be simpler. As a user, I want to be able to visualize my average plate (in terms of percentages of food groups represented) and see how close I am to the CFG recommendations. This can be done with a visualization of the recommended dish by the CFG.

## Use Case 8: Food Swap Effect Visualization
As a user I want to be able to visualize the effect of food swaps.
I should be able to choose different visualizations (e.g., bar plots or line charts for longitudinal changes), specific nutrients and specific time period (if I am visualizing the cumulative effects) and visualize the differences in nutrient intake before and after the swaps.
Similar visualizations can be done to see the adherence to the Canada Food Guide before and after the swaps. 