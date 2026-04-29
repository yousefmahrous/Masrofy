package view;

import controller.AuthManager;
import DataAccess.SQLiteHelper;
import model.UserProfile;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.List;

public class LoginScreen {
    private Stage stage;
    private PasswordField pinField;
    private UserProfile user; 

    public LoginScreen(Stage stage) {
        this.stage = stage;
        loadUser(); 
    }

    private void loadUser() {
        SQLiteHelper db = new SQLiteHelper();
        List<Object> results = db.query("SELECT * FROM app_user LIMIT 1");

        if (!results.isEmpty()) {
            this.user = (UserProfile) results.get(0);
        }
    }

    public void show() {
        if (user == null) {
            System.err.println("No user found, redirecting to setup...");
            return;
        }

        VBox root = new VBox(15);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-padding: 50; -fx-background-color: #ffffff;");

        Label welcomeLabel = new Label("Welcome back, " + user.getName());
        welcomeLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        pinField = new PasswordField();
        pinField.setPromptText("Enter PIN");
        pinField.setMaxWidth(200);
        pinField.setStyle("-fx-alignment: center;");

        Button loginBtn = new Button("Login");
        loginBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
        loginBtn.setMinWidth(100);

        loginBtn.setOnAction(e -> handleLogin());
        pinField.setOnAction(e -> handleLogin());

        root.getChildren().addAll(welcomeLabel, new Label("Enter your PIN to continue:"), pinField, loginBtn);

        stage.setScene(new Scene(root, 400, 350));
        stage.setTitle("Masrofy - Login");
        stage.show();
    }

    private void handleLogin() {
        try {
            if (pinField.getText().isEmpty()) return;

            int enteredPin = Integer.parseInt(pinField.getText());

            AuthManager auth = new AuthManager(user.getHashedPIN());

            if (auth.authenticate(enteredPin)) {
                new DashboardView(stage, user.getName()).show();
            } else {
                showError("Wrong PIN", "The PIN you entered is incorrect.");
                pinField.clear();
            }
        } catch (NumberFormatException e) {
            showError("Invalid Input", "Please enter a valid numeric PIN.");
        }
    }

    private void showError(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}