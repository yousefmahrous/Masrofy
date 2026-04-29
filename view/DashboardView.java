package view;

import controller.FinanceController;
import controller.Notificationmanager;
import DataAccess.SQLiteHelper;
import model.BudgetCycle;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class DashboardView {
    private Stage stage;
    private String userName;
    private FinanceController financeController;

    public DashboardView(Stage stage, String userName) {
        this.stage = stage;
        this.userName = userName;
        this.financeController = new FinanceController(new SQLiteHelper(), new Notificationmanager());
    }

    public void show() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: #f4f7f6;");

        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER_RIGHT);
        Button settingsBtn = new Button("Change PIN");
        settingsBtn.setStyle("-fx-background-color: #34495e; -fx-text-fill: white;");
        settingsBtn.setOnAction(e -> openChangePinDialog());
        topBar.getChildren().add(settingsBtn);

        BudgetCycle cycle = financeController.getActiveCycle();
        
        Label welcome = new Label("Hello, " + userName);
        welcome.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        VBox infoCard = new VBox(15);
        infoCard.setPadding(new Insets(20));
        infoCard.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");

        if (cycle != null) {
            double progress = cycle.getCurrentSpent() / cycle.getTotalAllowance();
            
            Label limitLabel = new Label("Daily Limit: " + String.format("%.2f EGP", cycle.getDailyLimit()));
            limitLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2ecc71;");

            ProgressBar progressBar = new ProgressBar(progress);
            progressBar.setPrefWidth(400);
            progressBar.setPrefHeight(20);
            
            if (progress >= 0.8) progressBar.setStyle("-fx-accent: #e74c3c;");
            else progressBar.setStyle("-fx-accent: #3498db;");

            Label spentLabel = new Label(String.format("Spent: %.2f / %.2f EGP", cycle.getCurrentSpent(), cycle.getTotalAllowance()));
            
            infoCard.getChildren().addAll(limitLabel, new Label("Overall Budget Progress:"), progressBar, spentLabel);
        } else {
            infoCard.getChildren().add(new Label("No Active Budget Cycle."));
        }

        HBox actions = new HBox(15);
        actions.setAlignment(Pos.CENTER);
        Button addBtn = new Button("Add Transaction");
        Button historyBtn = new Button("View History");
        
        addBtn.setOnAction(e -> new TransactionForm(stage, userName).show());
        historyBtn.setOnAction(e -> new HistoryScreen(stage, userName).show());
        
        actions.getChildren().addAll(addBtn, historyBtn);

        root.getChildren().addAll(topBar, welcome, infoCard, actions);
        stage.setScene(new Scene(root, 500, 600));
        stage.show();
    }

    private void openChangePinDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Security");
        dialog.setHeaderText("Change your 4-digit PIN");

        dialog.showAndWait().ifPresent(newPin -> {
            if (newPin.matches("\\d{4}")) {
                if (financeController.updateUserPin(userName, newPin)) {
                    Alert success = new Alert(Alert.AlertType.INFORMATION, "PIN Changed! Next time use your new PIN.");
                    success.show();
                } else {
                    new Alert(Alert.AlertType.ERROR, "Database Error! Check table name.").show();
                }
            } else {
                new Alert(Alert.AlertType.ERROR, "PIN must be exactly 4 digits!").show();
            }
        });
    }
}