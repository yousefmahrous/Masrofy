package controller;

import DataAccess.*;
import model.*;
import javafx.scene.control.Alert;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;
 
public class FinanceController {
 
    private final IDatabase db;
    private final Notificationmanager notificationManager;
 
    public FinanceController(IDatabase db, Notificationmanager notificationManager) {
        this.db = db;
        this.notificationManager = notificationManager;
    }

    public List<Category> getAllCategories() {
        return db.query("SELECT * FROM categories").stream()
                .map(o -> (Category) o)
                .collect(Collectors.toList());
    }
 
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

    public List<Transaction> getAllTransactions() {
        String sql = "SELECT t.*, c.name AS cat_name FROM transactions t JOIN categories c ON t.category_id = c.id ORDER BY t.time DESC";
        return db.query(sql).stream().map(o -> (Transaction) o).collect(Collectors.toList());
    }
 
    public BudgetCycle getActiveCycle() {
        List<Object> results = db.query("SELECT * FROM budget_cycle ORDER BY id DESC LIMIT 1");
        return results.isEmpty() ? null : (BudgetCycle) results.get(0);
    }
    
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
}