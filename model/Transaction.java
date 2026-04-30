package model;

import java.time.LocalDateTime;
        
/**
 * Represents a financial transaction in the Masrofy application.
 * Can be either an INCOME or EXPENSE with associated category and budget cycle.
 *
 * @author Masrofy Development Team
 * @version 1.0
 */
public class Transaction {
    private int id;
    private int categoryID;
    private int budgetCycleId;
    private double amount;
    private String notes; 
    private LocalDateTime time = LocalDateTime.now();
    private Category category;
    private TransactionType type;

    /**
     * Constructs a new Transaction without ID (for new transactions).
     */
    public Transaction(int categoryID, int budgetCycleId, double amount, String notes, Category category, TransactionType type) {
        this.categoryID = categoryID;
        this.budgetCycleId = budgetCycleId;
        this.amount = amount;
        this.notes = notes;
        this.category = category;
        this.type = type;
    }

    /**
     * Constructs a Transaction with full details including ID (for loaded transactions).
     */
    public Transaction(int id, int categoryID, int budgetCycleId, double amount, String notes, LocalDateTime time, Category category, TransactionType type) {
        this.id = id;
        this.categoryID = categoryID;
        this.budgetCycleId = budgetCycleId;
        this.amount = amount;
        this.notes = notes;
        this.time = time;
        this.category = category;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public int getCategoryId() {
        return categoryID;
    }

    public int getBudgetCycleId() {
        return budgetCycleId;
    }

    public double getAmount() {
        return amount;
    }

    public String getNotes() {
        return notes;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public Category getCategory() {
        return category;
    }

    public TransactionType getType() {
        return type;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCategoryID(int categoryID) {
        this.categoryID = categoryID;
    }

    public void setBudgetCycleId(int budgetCycleId) {
        this.budgetCycleId = budgetCycleId;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }
}