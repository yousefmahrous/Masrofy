package controller;

import DataAccess.*;
import model.*;
import javafx.scene.control.Alert;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Main controller class that handles all financial operations in the Masrofy application.
 * Manages budget cycles, transactions, categories, and coordinates with the database and notification system.
 *
 * @author Masrofy Development Team
 * @version 1.0
 */
public class FinanceController {

    private final IDatabase db;
    private final NotificationManager notificationManager;

    /**
     * Constructs a new FinanceController with database and notification manager.
     *
     * @param db the database implementation
     * @param notificationManager the notification manager instance
     */
    public FinanceController(IDatabase db, NotificationManager notificationManager) {
        this.db = db;
        this.notificationManager = notificationManager;
    }

    /**
     * Retrieves all categories from the database.
     *
     * @return list of all Category objects
     */
    public List<Category> getAllCategories() {
        return db.query("SELECT * FROM categories").stream()
                .map(o -> (Category) o)
                .collect(Collectors.toList());
    }

    /**
     * Adds a new transaction and updates the active budget cycle and category spending accordingly.
     * Performs budget checks and sends notifications when necessary.
     *
     * @param t the transaction to be added
     */
    public void addTransaction(Transaction t) {
        BudgetCycle cycle = getActiveCycle();
        if (cycle == null) return;

        if (t.getType() == TransactionType.EXPENSE) {
            double newSpent = cycle.getCurrentSpent() + t.getAmount();
            double totalAllowance = cycle.getTotalAllowance();

            if (newSpent > totalAllowance) {
                notificationManager.sendAlert("Balance is insufficient.");
                return; 
            }

            if (newSpent >= totalAllowance * 0.8 && newSpent < totalAllowance) {
                notificationManager.sendAlert("Alert : You Exceeded 80% of your budget!");
            }

            if (newSpent == totalAllowance) {
                notificationManager.sendAlert("Attention! You have consumed 100% of your budget.");
            }

            cycle.setCurrentSpent(newSpent);
        } else {
            cycle.setTotalAllowance(cycle.getTotalAllowance() + t.getAmount());
        }

        updateAndSaveCycle(cycle); 

        db.insert("transactions", t);
        updateCategorySpent(t, t.getAmount(), true);
    }
    
    /**
     * Updates an existing transaction and adjusts budget cycle and category values.
     *
     * @param t the transaction with updated values
     * @param oldAmount the previous amount before update
     * @param newAmount the new amount after update
     * @return true if the update was successful, false otherwise
     */
    public boolean updateTransaction(Transaction t, double oldAmount, double newAmount) {
        BudgetCycle cycle = getActiveCycle();
        if (cycle == null) return false;

        double diff = newAmount - oldAmount;
        if (t.getType() == TransactionType.EXPENSE) {
            double newSpent = cycle.getCurrentSpent() + diff;
            double totalAllowance = cycle.getTotalAllowance();

            if (newSpent > totalAllowance) {
                notificationManager.sendAlert("Balance is insufficient.");
                return false;
            }

            if (newSpent >= totalAllowance * 0.8 && newSpent < totalAllowance) {
                notificationManager.sendAlert("Alert : You Exceeded 80% of your budget!");
            }

            if (newSpent == totalAllowance) {
                notificationManager.sendAlert("Attention! You have consumed 100% of your budget.");
            }

            cycle.setCurrentSpent(newSpent);
        } else {
            cycle.setTotalAllowance(cycle.getTotalAllowance() + diff);
        }

        boolean success = db.update("transactions", t.getId(), t);
        updateAndSaveCycle(cycle);
        updateCategorySpent(t, diff, true);
        return success;
    }

    /**
     * Deletes a transaction and updates the budget cycle and category spending.
     *
     * @param t the transaction to delete
     * @return true if deletion was successful
     */
    public boolean deleteTransaction(Transaction t) {
        BudgetCycle cycle = getActiveCycle();
        if (cycle == null) return false;

        if (t.getType() == TransactionType.EXPENSE) {
            cycle.setCurrentSpent(cycle.getCurrentSpent() - t.getAmount());
        } else {
            cycle.setTotalAllowance(cycle.getTotalAllowance() - t.getAmount());
        }

        db.delete("transactions", t.getId());
        updateAndSaveCycle(cycle);
        updateCategorySpent(t, t.getAmount(), false);
        return true;
    }

    /**
     * Updates the daily limit of the budget cycle and saves it to the database.
     *
     * @param cycle the budget cycle to update and save
     */
    private void updateAndSaveCycle(BudgetCycle cycle) {
        long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), cycle.getEndDate());
        if (daysLeft <= 0) daysLeft = 1; 

        double remainingMoney = cycle.getTotalAllowance() - cycle.getCurrentSpent();

        if (remainingMoney <= 0) {
            cycle.setDailyLimit(0);
        } else {
            cycle.setDailyLimit(remainingMoney / daysLeft);
        }

        db.update("budget_cycle", cycle.getId(), cycle);
    }

    /**
     * Updates the current spent amount of a category and optionally checks its budget limit.
     *
     * @param t the transaction affecting the category
     * @param amount the amount to add or subtract
     * @param isAdd true if adding, false if subtracting
     */
    private void updateCategorySpent(Transaction t, double amount, boolean isAdd) {
        List<Object> res = db.query("SELECT * FROM categories WHERE id=" + t.getCategoryId());
        if (!res.isEmpty()) {
            Category c = (Category) res.get(0);
            if (t.getType() == TransactionType.EXPENSE) {
                c.setCurrentSpent(isAdd ? c.getCurrentSpent() + amount : c.getCurrentSpent() - amount);
                db.update("categories", c.getId(), c);
                if (isAdd) notificationManager.checkCategoryLimit(c.getCurrentSpent(), c.getBudgetLimit());
            }
        }
    }

    /**
     * Retrieves all transactions with their associated category names.
     *
     * @return list of all transactions
     */
    public List<Transaction> getAllTransactions() {
        String sql = "SELECT t.*, c.name AS cat_name FROM transactions t JOIN categories c ON t.category_id = c.id ORDER BY t.time DESC";
        return db.query(sql).stream().map(o -> (Transaction) o).collect(Collectors.toList());
    }

    /**
     * Gets the most recent active budget cycle.
     *
     * @return the active BudgetCycle, or null if none exists
     */
    public BudgetCycle getActiveCycle() {
        List<Object> results = db.query("SELECT * FROM budget_cycle ORDER BY id DESC LIMIT 1");
        return results.isEmpty() ? null : (BudgetCycle) results.get(0);
    }
    
    /**
     * Updates the user's PIN in the database.
     *
     * @param userName the username whose PIN will be updated
     * @param newPin the new 4-digit PIN as string
     * @return true if the update was successful
     */
    public boolean updateUserPin(String userName, String newPin) {
        try {
            AuthManager auth = new AuthManager(""); 
            String hashed = auth.hash(Integer.parseInt(newPin));

            String sql = "UPDATE app_user SET pin_hash = '" + hashed + "' WHERE name = '" + userName + "'";

            if (db instanceof DataAccess.SQLiteHelper) {
                ((DataAccess.SQLiteHelper) db).executeNonQuery(sql);
                return true;
            }
            return false;
        } catch (Exception e) {
            System.out.println("Update PIN Error: " + e.getMessage());
            return false;
        }
    }
    
        /**
     * Checks if the current budget cycle has ended.
     * If the cycle has ended, clears all application data and returns true.
     * 
     * @return true if cycle ended and data was cleared, false otherwise
     */
    public boolean checkAndHandleCycleExpiration() {
        BudgetCycle activeCycle = getActiveCycle();

        if (activeCycle == null) {
            return false;
        }

        LocalDate today = LocalDate.now();
        LocalDate endDate = activeCycle.getEndDate();

        if (endDate.isBefore(today) || endDate.isEqual(today)) {
            clearAllApplicationData();
            return true;
        }

        return false;
    }

    /**
     * Clears all data from all tables to reset the application state.
     * This is called when a budget cycle ends.
     */
    private void clearAllApplicationData() {
        if (db instanceof DataAccess.SQLiteHelper) {
            ((DataAccess.SQLiteHelper) db).clearAllTables();
        }
    }

    /**
     * Checks if any user exists in the database.
     * 
     * @return true if at least one user exists, false otherwise
     */
    public boolean hasExistingUser() {
        List<Object> users = db.query("SELECT * FROM app_user");
        return !users.isEmpty();
    }
}
