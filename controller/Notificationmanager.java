package controller;

import javafx.scene.control.Alert;

/**
 * Manages all notification and alert functionality in the Masrofy budgeting application.
 * This class is responsible for showing budget warnings and category limit alerts
 * using JavaFX Alert dialogs on the correct application thread.
 *
 * @author Masrofy Development Team
 * @version 1.0
 */
public class NotificationManager {
    
    /**
     * Sends a warning alert message to the user using JavaFX Alert.
     * Ensures the alert is shown on the JavaFX Application Thread using Platform.runLater.
     *
     * @param msg the message content to display in the alert dialog
     */
    public void sendAlert(String msg) {
        javafx.application.Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Budget Alert");
            alert.setHeaderText(null);
            alert.setContentText(msg);
            alert.showAndWait();
        });
    }

    /**
     * Checks the spending against a category's budget limit and triggers appropriate alerts.
     * Alerts are sent when spending reaches or exceeds 100% or when it reaches 80% of the limit.
     *
     * @param catSpent the current amount spent in the category
     * @param catLimit the maximum budget limit for the category
     */
    public void checkCategoryLimit(double catSpent, double catLimit) {
        if (catLimit <= 0) return;
        double percent = (catSpent / catLimit) * 100;
        if (catSpent >= catLimit) {
            sendAlert("Category budget exceeded! Spent: " + catSpent + " / Limit: " + catLimit);
        } else if (percent >= 80) {
            sendAlert(String.format("Category is at %.1f%% of its budget limit.", percent));
        }
    }
}