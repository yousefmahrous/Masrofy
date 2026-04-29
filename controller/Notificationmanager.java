package controller;

import javafx.scene.control.Alert;

public class Notificationmanager {
    
public void sendAlert(String msg) {
    javafx.application.Platform.runLater(() -> {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Budget Alert");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    });
}

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