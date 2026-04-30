package model;

import java.util.ArrayList;

/**
 * Represents a spending category in the Masrofy budgeting application.
 * Each category has a name, budget limit, and tracks its current spending.
 *
 * @author Masrofy Development Team
 * @version 1.0
 */
public class Category {
    private int id;
    private String name;
    private double budgetLimit;
    private double currentSpent;
    private ArrayList<Transaction> transactions = new ArrayList();

    /**
     * Constructs a new Category with name and budget limit.
     *
     * @param name the name of the category
     * @param budgetLimit the maximum budget allocated to this category
     */
    public Category(String name, double budgetLimit) {
        this.name = name;
        this.budgetLimit = budgetLimit;
        this.currentSpent = 0.0;
    }

    /**
     * Full constructor used when loading from database.
     */
    public Category(int id, String name, double budgetLimit, double currentSpent) {
        this.id = id;
        this.name = name;
        this.budgetLimit = budgetLimit;
        this.currentSpent = currentSpent;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getBudgetLimit() {
        return budgetLimit;
    }

    public double getCurrentSpent() {
        return currentSpent;
    }

    public ArrayList<Transaction> getTransactions() {
        return transactions;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBudgetLimit(double budgetLimit) {
        this.budgetLimit = budgetLimit;
    }

    public void setCurrentSpent(double currentSpent) {
        this.currentSpent = currentSpent;
    }

    public void setTransactions(ArrayList<Transaction> transactions) {
        this.transactions = transactions;
    }
}