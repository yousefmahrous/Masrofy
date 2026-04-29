package view;

import controller.*;
import DataAccess.SQLiteHelper;
import model.Transaction;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.List;

public class HistoryScreen {
    private Stage stage;
    private String userName;
    private FinanceController financeController;
    private TableView<Transaction> table;

    public HistoryScreen(Stage stage, String userName) {
        this.stage = stage;
        this.userName = userName;
        this.financeController = new FinanceController(new SQLiteHelper(), new Notificationmanager());
    }

    public void show() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f8f9fa;");

        Label title = new Label("Transactions Log");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        TableColumn<Transaction, String> catCol = new TableColumn<>("Category");
        catCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().getCategory() != null ? d.getValue().getCategory().getName() : "General"
        ));

        TableColumn<Transaction, Double> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("amount"));
        amountCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(amount + " EGP");
                    Transaction t = getTableView().getItems().get(getIndex());
                    if (t.getType() == model.TransactionType.EXPENSE) {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    }
                }
            }
        });

        TableColumn<Transaction, Void> actionCol = new TableColumn<>("Manage");
        actionCol.setCellFactory(p -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button delBtn = new Button("Delete");
            private final HBox container = new HBox(10, editBtn, delBtn);
            {
                container.setAlignment(Pos.CENTER);
                editBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand;");
                delBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand;");
                
                delBtn.setOnAction(e -> {
                    Transaction t = getTableView().getItems().get(getIndex());
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                        "Are you sure you want to delete this? This will update your daily limit.",
                        ButtonType.YES, ButtonType.NO);
                    confirm.showAndWait().ifPresent(btn -> {
                        if (btn == ButtonType.YES && financeController.deleteTransaction(t)) {
                            refreshTable();
                        }
                    });
                });

                editBtn.setOnAction(e -> handleEdit(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void i, boolean e) {
                super.updateItem(i, e);
                setGraphic(e ? null : container);
            }
        });

        table.getColumns().addAll(catCol, amountCol, actionCol);
        refreshTable();

        Button back = new Button("Back to Dashboard");
        back.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white;");
        back.setOnAction(e -> new DashboardView(stage, userName).show());

        root.getChildren().addAll(title, table, back);
        stage.setScene(new Scene(root, 700, 500));
    }

    private void handleEdit(Transaction t) {
        double oldVal = t.getAmount();
        TextInputDialog dialog = new TextInputDialog(String.valueOf(oldVal));
        dialog.setTitle("Edit Amount");
        dialog.setHeaderText("Transaction: " + (t.getCategory() != null ? t.getCategory().getName() : "Item"));
        dialog.setContentText("New Amount:");

        dialog.showAndWait().ifPresent(input -> {
            try {
                double newVal = Double.parseDouble(input);
                t.setAmount(newVal);
                if (financeController.updateTransaction(t, oldVal, newVal)) {
                    refreshTable();
                }
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "Invalid Input").show();
            }
        });
    }

    private void refreshTable() {
       List<Transaction> list = financeController.getAllTransactions();
       if (list.isEmpty()) {
           table.setPlaceholder(new Label("No Transactions Found. Log your first expense!"));
       }
       table.setItems(FXCollections.observableArrayList(list));
       table.refresh();
   }
}