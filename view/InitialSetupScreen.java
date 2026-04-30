package view;

import controller.AuthManager;
import model.*;
import DataAccess.SQLiteHelper;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.time.temporal.ChronoUnit;
import java.time.LocalDate;

/**
 * Initial setup screen shown when the application is launched for the first time.
 * Allows the user to create their profile, set a PIN, and define the initial budget cycle.
 *
 * @author Masrofy Development Team
 * @version 1.0
 */
public class InitialSetupScreen {
    private Stage stage;

    /**
     * Constructs a new InitialSetupScreen.
     *
     * @param stage the primary stage
     */
    public InitialSetupScreen(Stage stage) { this.stage = stage; }

    /**
     * Displays the initial setup form for user registration and budget initialization.
     */
    public void show() {
        VBox root = new VBox(12);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-padding: 30; -fx-background-color: #f4f4f4;");

        Label userLabel = new Label("User Setup");
        userLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        
        TextField nameField = new TextField();
        nameField.setPromptText("Name");
        
        PasswordField pinField = new PasswordField();
        pinField.setPromptText("4-digit PIN");

        Label budgetLabel = new Label("Initial Budget");
        budgetLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        
        TextField allowanceField = new TextField();
        allowanceField.setPromptText("Monthly Budget (e.g., 5000)");

        DatePicker datePicker = new DatePicker(LocalDate.now().plusMonths(1));
        datePicker.setPromptText("End Date");

        Button startBtn = new Button("Finish Setup");
        startBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold;");
        startBtn.setMinWidth(200);

        startBtn.setOnAction(e -> {
            try {
                String name = nameField.getText();
                String pin = pinField.getText();
                String allowanceStr = allowanceField.getText();
                LocalDate endDate = datePicker.getValue();
                
                if (name.isEmpty() || pin.length() < 4 || allowanceStr.isEmpty() || endDate == null) {
                     throw new Exception("Missing data");
                }

                double allowance = Double.parseDouble(allowanceStr);
                if (allowance <= 0) throw new Exception("Invalid allowance"); 
                
                saveUserData(name, pin, allowance, endDate);
                
            } catch (Exception ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Please fill all fields correctly!\nPIN must be 4+ digits.");
                alert.show();
            }
        });

        root.getChildren().addAll(userLabel, nameField, pinField, 
                                 new Separator(), 
                                 budgetLabel, allowanceField, datePicker, 
                                 new Separator(), startBtn);

        stage.setScene(new Scene(root, 400, 500));
        stage.setTitle("Masrofy - Initial Setup");
        stage.show();
    }

    /**
     * Saves the initial user data, hashed PIN, and creates the first budget cycle.
     *
     * @param name the user's name
     * @param pin the chosen PIN
     * @param allowance the initial budget allowance
     * @param endDate the end date of the first budget cycle
     */
    private void saveUserData(String name, String pin, double allowance, LocalDate endDate) {
        SQLiteHelper db = new SQLiteHelper();

        AuthManager auth = new AuthManager("");
        String hashedPin = auth.hash(Integer.parseInt(pin));
        db.insert("app_user", new UserProfile(name, hashedPin));

        BudgetCycle firstCycle = new BudgetCycle(allowance, endDate);
        long days = ChronoUnit.DAYS.between(LocalDate.now(), endDate);
        firstCycle.setDailyLimit(allowance / (days <= 0 ? 1 : days));
        db.insert("budget_cycle", firstCycle);

        new DashboardView(stage, name).show();
    }
}