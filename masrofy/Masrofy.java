package masrofy;

import model.*;
import view.*;
import DataAccess.*;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Masrofy extends Application {
    @Override
    public void start(Stage primaryStage) {
        SQLiteHelper db = new SQLiteHelper();
        db.onCreate();

        if (db.query("SELECT * FROM app_user").isEmpty()) {
            new InitialSetupScreen(primaryStage).show();
        } else {
            new LoginScreen(primaryStage).show();
        }
    }
    public static void main(String[] args) {
        launch(args);
    }
}