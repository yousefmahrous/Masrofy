package model;

import java.time.LocalDate;
import java.util.ArrayList;

/**
 * Represents a budget cycle (usually monthly) in the Masrofy application.
 * Tracks total allowance, current spending, daily limit, and period dates.
 *
 * @author Masrofy Development Team
 * @version 1.0
 */
public class BudgetCycle {
    private int id;
    private double totalAllowance;
    private double currentSpent;
    private LocalDate startDate = LocalDate.now();
    private LocalDate endDate;
    private final int thresholdPercent = 80;
    private double dailyLimit; 
    private ArrayList<Transaction> transactions = new ArrayList<>();
    
    /**
     * Constructs a new BudgetCycle with total allowance and end date.
     */
    public BudgetCycle(double totalAllowance, LocalDate endDate) {
        this.totalAllowance = totalAllowance;
        this.endDate = endDate;
        this.currentSpent = 0.0;
    }

    /**
     * Full constructor used when loading from database.
     */
    public BudgetCycle(int id, double totalAllowance, double currentSpent, LocalDate startDate, LocalDate endDate, double dailyLimit) {
        this.id = id;
        this.totalAllowance = totalAllowance;
        this.currentSpent = currentSpent;
        this.startDate = startDate; 
        this.endDate = endDate;
        this.dailyLimit = dailyLimit;
    }
    
    public int getId() {
        return id;
    }

    public double getTotalAllowance() {
        return totalAllowance;
    }

    public double getCurrentSpent() {
        return currentSpent;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public int getThresholdPercent() {
        return thresholdPercent;
    }

    public double getDailyLimit() {
        return dailyLimit;
    }

    public ArrayList<Transaction> getTransactions() {
        return transactions;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTotalAllowance(double totalAllowance) {
        this.totalAllowance = totalAllowance;
    }

    public void setCurrentSpent(double currentSpent) {
        this.currentSpent = currentSpent;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public void setDailyLimit(double dailyLimit) {
        this.dailyLimit = dailyLimit;
    }

    public void setTransactions(ArrayList<Transaction> transactions) {
        this.transactions = transactions;
    }
}