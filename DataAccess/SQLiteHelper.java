package DataAccess;

import javafx.scene.control.Alert;
import java.sql.*;
import model.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * SQLite database implementation of the {@link IDatabase} interface.
 * Handles all database operations for the Masrofy application including
 * CRUD operations and table creation.
 *
 * @author Masrofy Development Team
 * @version 1.0
 */
public class SQLiteHelper implements IDatabase {
    private static final String url = "jdbc:sqlite:Masrofy.db";

    /**
     * Displays an error alert to the user.
     *
     * @param title the title of the error alert
     * @param content the error message content
     */
    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Establishes a connection to the SQLite database.
     *
     * @return Connection object to the database
     * @throws SQLException if a database access error occurs
     */
    private Connection connect() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            showError("Driver Error", "Driver not found!");
        }
        return DriverManager.getConnection(url);
    }
    
    /**
     * Creates the necessary database tables if they do not already exist.
     */
    @Override
    public void onCreate() {
        try (Connection conn = this.connect(); Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS budget_cycle (id INTEGER PRIMARY KEY AUTOINCREMENT, total_allowance REAL, current_spent REAL, start_date TEXT, end_date TEXT, daily_limit REAL)");
            stmt.execute("CREATE TABLE IF NOT EXISTS categories (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, budget_limit REAL, current_spent REAL)");
            stmt.execute("CREATE TABLE IF NOT EXISTS transactions (id INTEGER PRIMARY KEY AUTOINCREMENT, category_id INTEGER, budget_id INTEGER, amount REAL, notes TEXT, time TEXT, type TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS app_user (name TEXT, pin_hash TEXT)");
        } catch (SQLException e) {
            showError("Database Error", e.getMessage());
        }
    }

    /**
     * Inserts data into the appropriate table based on the object type.
     */
    @Override
    public boolean insert(String table, Object data) {
        try (Connection conn = this.connect()) {
            if (data instanceof Transaction) {
                Transaction t = (Transaction) data;
                PreparedStatement pstmt = conn.prepareStatement("INSERT INTO transactions(category_id, budget_id, amount, notes, time, type) VALUES(?,?,?,?,?,?)");
                pstmt.setInt(1, t.getCategoryId());
                pstmt.setInt(2, t.getBudgetCycleId());
                pstmt.setDouble(3, t.getAmount());
                pstmt.setString(4, t.getNotes());
                pstmt.setString(5, t.getTime().toString());
                pstmt.setString(6, t.getType().name());
                return pstmt.executeUpdate() > 0;
            } else if (data instanceof Category) {
                Category c = (Category) data;
                PreparedStatement pstmt = conn.prepareStatement("INSERT INTO categories(name, budget_limit, current_spent) VALUES(?,?,?)");
                pstmt.setString(1, c.getName());
                pstmt.setDouble(2, c.getBudgetLimit());
                pstmt.setDouble(3, c.getCurrentSpent());
                return pstmt.executeUpdate() > 0;
            } else if (data instanceof BudgetCycle) {
                BudgetCycle bc = (BudgetCycle) data;
                PreparedStatement pstmt = conn.prepareStatement("INSERT INTO budget_cycle(total_allowance, current_spent, start_date, end_date, daily_limit) VALUES(?,?,?,?,?)");
                pstmt.setDouble(1, bc.getTotalAllowance());
                pstmt.setDouble(2, bc.getCurrentSpent());
                pstmt.setString(3, bc.getStartDate().toString());
                pstmt.setString(4, bc.getEndDate().toString());
                pstmt.setDouble(5, bc.getDailyLimit());
                return pstmt.executeUpdate() > 0;
            } else if (data instanceof UserProfile) {
                UserProfile up = (UserProfile) data;
                PreparedStatement pstmt = conn.prepareStatement("INSERT INTO app_user(name, pin_hash) VALUES(?,?)");
                pstmt.setString(1, up.getName());
                pstmt.setString(2, up.getHashedPIN());
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            showError("Insert Error", e.getMessage());
        }
        return false;
    }

    /**
     * Updates a record in the database based on the object type.
     */
    @Override
    public boolean update(String table, int id, Object data) {
        try (Connection conn = this.connect()) {
            if (data instanceof BudgetCycle) {
                BudgetCycle bc = (BudgetCycle) data;
                PreparedStatement pstmt = conn.prepareStatement("UPDATE budget_cycle SET total_allowance=?, current_spent=?, daily_limit=? WHERE id=?");
                pstmt.setDouble(1, bc.getTotalAllowance());
                pstmt.setDouble(2, bc.getCurrentSpent());
                pstmt.setDouble(3, bc.getDailyLimit());
                pstmt.setInt(4, id);
                return pstmt.executeUpdate() > 0;
            } else if (data instanceof Category) {
                Category c = (Category) data;
                PreparedStatement pstmt = conn.prepareStatement("UPDATE categories SET current_spent=? WHERE id=?");
                pstmt.setDouble(1, c.getCurrentSpent());
                pstmt.setInt(2, id);
                return pstmt.executeUpdate() > 0;
            } else if (data instanceof Transaction) {
                Transaction t = (Transaction) data;
                PreparedStatement pstmt = conn.prepareStatement("UPDATE transactions SET amount=?, notes=? WHERE id=?");
                pstmt.setDouble(1, t.getAmount());
                pstmt.setString(2, t.getNotes());
                pstmt.setInt(3, id);
                return pstmt.executeUpdate() > 0;
            }
            return false;
        } catch (SQLException e) {
            showError("Update Error", e.getMessage());
            return false;
        }
    }

    /**
     * Deletes a record from the specified table by its ID.
     */
    @Override
    public boolean delete(String table, int id) {
        try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement("DELETE FROM " + table + " WHERE id = ?")) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            showError("Delete Error", e.getMessage());
            return false;
        }
    }

    /**
     * Executes a SELECT query and maps the results to appropriate model objects.
     */
    @Override
    public List<Object> query(String sql) {
        List<Object> results = new ArrayList<>();
        try (Connection conn = this.connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            String sqlLower = sql.toLowerCase();
            while (rs.next()) {
                if (sqlLower.contains("from transactions") || sqlLower.contains("cat_name")) {
                    Category cat = new Category(rs.getInt("category_id"), rs.getString("cat_name"), 0, 0);
                    results.add(new Transaction(rs.getInt("id"), rs.getInt("category_id"), rs.getInt("budget_id"), rs.getDouble("amount"), rs.getString("notes"), LocalDateTime.parse(rs.getString("time")), cat, TransactionType.valueOf(rs.getString("type"))));
                } else if (sqlLower.contains("from categories")) {
                    results.add(new Category(rs.getInt("id"), rs.getString("name"), rs.getDouble("budget_limit"), rs.getDouble("current_spent")));
                } else if (sqlLower.contains("from budget_cycle")) {
                    results.add(new BudgetCycle(rs.getInt("id"), rs.getDouble("total_allowance"), rs.getDouble("current_spent"), LocalDate.parse(rs.getString("start_date")), LocalDate.parse(rs.getString("end_date")), rs.getDouble("daily_limit")));
                } else if (sqlLower.contains("from app_user")) {
                    results.add(new UserProfile(rs.getString("name"), rs.getString("pin_hash")));
                }
            }
        } catch (SQLException e) {
            showError("Query Error", e.getMessage());
        }
        return results;
    }

    /**
     * Executes a non-query SQL statement (UPDATE, INSERT, DELETE) without returning results.
     *
     * @param sql the SQL statement to execute
     */
    public void executeNonQuery(String sql) {
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            System.err.println("Execution Error: " + e.getMessage());
        }
    }
}