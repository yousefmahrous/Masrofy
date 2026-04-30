package view;

import controller.FinanceController;
import controller.NotificationManager;
import DataAccess.SQLiteHelper;
import model.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.List;
import java.util.stream.Collectors;

/**
 * UI Form for adding new income or expense transactions.
 * Allows user to select or create a category and enter transaction details.
 *
 * @author Masrofy Development Team
 * @version 1.0
 */
public class TransactionForm {
    private Stage stage;
    private String userName;
    private FinanceController financeController;

    /**
     * Constructs a new TransactionForm.
     *
     * @param stage the primary stage
     * @param userName the current logged-in username
     */
    public TransactionForm(Stage stage, String userName) {
        this.stage = stage;
        this.userName = userName;
        this.financeController = new FinanceController(new SQLiteHelper(), new NotificationManager());
    }

    /**
     * Displays the transaction form window.
     */
    public void show() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(25));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #ffffff;");

        Label title = new Label("Add Transaction");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        TextField amountField = new TextField();
        amountField.setPromptText("Amount (EGP)");
        amountField.setMaxWidth(250);

        TextField categoryField = new TextField();
        categoryField.setPromptText("Category (e.g. Food, Car)");
        categoryField.setMaxWidth(250);

        ContextMenu suggestionsMenu = new ContextMenu();
        List<Category> existingCats = financeController.getAllCategories();

        categoryField.textProperty().addListener((obs, old, newValue) -> {
            suggestionsMenu.getItems().clear();
            if (!newValue.isEmpty()) {
                List<Category> matches = existingCats.stream()
                        .filter(c -> c.getName().toLowerCase().contains(newValue.toLowerCase()))
                        .collect(Collectors.toList());
                for (Category cat : matches) {
                    MenuItem item = new MenuItem(cat.getName());
                    item.setOnAction(e -> categoryField.setText(cat.getName()));
                    suggestionsMenu.getItems().add(item);
                }
                if (!suggestionsMenu.getItems().isEmpty()) {
                    suggestionsMenu.show(categoryField, javafx.geometry.Side.BOTTOM, 0, 0);
                } else { suggestionsMenu.hide(); }
            } else { suggestionsMenu.hide(); }
        });

        TextField notesField = new TextField();
        notesField.setPromptText("Notes");
        notesField.setMaxWidth(250);

        ToggleGroup typeGroup = new ToggleGroup();
        RadioButton rbExpense = new RadioButton("Expense");
        rbExpense.setToggleGroup(typeGroup);
        rbExpense.setSelected(true);
        RadioButton rbIncome = new RadioButton("Income");
        rbIncome.setToggleGroup(typeGroup);

        Button saveBtn = new Button("Save Transaction");
        saveBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold;");
        saveBtn.setMinWidth(200);

        Button backBtn = new Button("Back to Dashboard");
        backBtn.setMinWidth(200);
        backBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold;");

        saveBtn.setOnAction(e -> {
            try {
                double amount = Double.parseDouble(amountField.getText());
                String catName = categoryField.getText().trim();
                BudgetCycle currentCycle = financeController.getActiveCycle();

                if (catName.isEmpty() || currentCycle == null) {
                    new Alert(Alert.AlertType.WARNING, "Please fill all fields!").show();
                    return;
                }

                Category targetCat = existingCats.stream()
                        .filter(c -> c.getName().equalsIgnoreCase(catName))
                        .findFirst().orElse(null);

                if (targetCat == null) {
                    SQLiteHelper helper = new SQLiteHelper();
                    helper.insert("categories", new Category(catName, 0));
                    List<Object> results = helper.query("SELECT * FROM categories WHERE name='" + catName + "'");
                    if(!results.isEmpty()) {
                        targetCat = (Category) results.get(0);
                    }
                }

                Transaction t = new Transaction(targetCat.getId(), currentCycle.getId(), amount, 
                        notesField.getText(), targetCat, rbExpense.isSelected() ? TransactionType.EXPENSE : TransactionType.INCOME);

                financeController.addTransaction(t);
                new DashboardView(stage, userName).show();
            } catch (NumberFormatException ex) {
                new Alert(Alert.AlertType.ERROR, "Please enter a valid amount!").show();
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "System Error: " + ex.getMessage()).show();
            }
        });

        backBtn.setOnAction(e -> new DashboardView(stage, userName).show());
        root.getChildren().addAll(title, amountField, categoryField, notesField, rbExpense, rbIncome, saveBtn, backBtn);
        stage.setScene(new Scene(root, 400, 500));
        stage.show();
    }
}