package view;

import controller.MealLoggerController;
import controller.UserProfileController;
import model.meal.Meal;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

public class JournalPanel extends JPanel {
    private JTable table;
    private MealLoggerController controller = new MealLoggerController();
    private UserProfileController userController = new UserProfileController();
    private List<Meal> meals;
    private DefaultTableModel model;

    public JournalPanel(int userId) {
        setLayout(new BorderLayout(10, 10));
        setBorder(new TitledBorder("Meal Journal"));

        JLabel titleLabel = new JLabel("Your Meal History", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        add(titleLabel, BorderLayout.NORTH);

        // Top panel with "Add Meal" button
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addMealBtn = new JButton("Add Meal");
        addMealBtn.addActionListener(e -> {
            JFrame frame = new JFrame("Add New Meal");
            MealEntryPanel mealEntryPanel = new MealEntryPanel();

            frame.setContentPane(mealEntryPanel);
            frame.setSize(500, 400);
            frame.setLocationRelativeTo(this);
            frame.setVisible(true);
        });

        topPanel.add(addMealBtn);
        add(topPanel, BorderLayout.SOUTH);

        // Table setup
        String[] columns = {"Date", "Meal Name", "Calories"};
        model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        //meals = controller.getMealsForUser(userController.getCurrentProfile().getUserID());
        populateTable();

        table = new JTable(model);
        table.setRowHeight(24);
        table.setFont(new Font("SansSerif", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                showMealDetails(table.getSelectedRow());
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);
    }

    private void populateTable() {
        model.setRowCount(0);  // Clear existing rows
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (Meal meal : meals) {
            model.addRow(new Object[]{
                    sdf.format(meal.getDate()),
                    meal.getType(),
                    meal.getCalories()
            });
        }
    }

    private void showMealDetails(int rowIndex) {
        Meal meal = meals.get(rowIndex);
        Map<String, Double> nutrients = meal.getNutrients();

        StringBuilder sb = new StringBuilder();
        sb.append("<html><h2>").append(meal.getType()).append(" - Nutrient Breakdown</h2><table>");

        for (Map.Entry<String, Double> entry : nutrients.entrySet()) {
            sb.append("<tr><td><b>").append(entry.getKey())
                    .append(":</b></td><td>").append(String.format("%.2f", entry.getValue()))
                    .append("</td></tr>");
        }

        sb.append("</table></html>");

        JLabel label = new JLabel(sb.toString());
        label.setFont(new Font("SansSerif", Font.PLAIN, 14));
        JOptionPane.showMessageDialog(this, label, "Meal Details", JOptionPane.INFORMATION_MESSAGE);
    }

}
class MealJournalApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Meal Journal");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(500, 300);
            frame.setLocationRelativeTo(null);
            frame.add(new JournalPanel(1)); // Pass dummy userId
            frame.setVisible(true);
        });
    }
}