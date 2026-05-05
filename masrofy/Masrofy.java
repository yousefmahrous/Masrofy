package masrofy;

import model.*;
import view.*;
import DataAccess.*;
import controller.FinanceController;
import controller.NotificationManager;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Main entry point for the Masrofy JavaFX budgeting application.
 * Handles initial database setup and decides whether to show login or initial setup screen.
 *
 * @author Masrofy Development Team
 * @version 1.0
 */
public class Masrofy extends Application {

    /**
     * Starts the JavaFX application.
     * Checks if a user exists; if not, shows initial setup, otherwise shows login screen.
     *
     * @param primaryStage the primary stage for this application
     */
    @Override
    public void start(Stage primaryStage) {
        SQLiteHelper db = new SQLiteHelper();
        db.onCreate();

        FinanceController financeController = new FinanceController(db, new NotificationManager());

        boolean cycleEnded = financeController.checkAndHandleCycleExpiration();

        if (!financeController.hasExistingUser()) {
            new InitialSetupScreen(primaryStage).show();
        } else {
            new LoginScreen(primaryStage).show();
        }
    }

    /**
     * Main method to launch the JavaFX application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
