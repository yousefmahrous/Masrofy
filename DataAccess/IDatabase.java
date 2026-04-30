package DataAccess;

import java.util.List;

/**
 * Interface defining the contract for all database operations in the Masrofy application.
 * Provides methods for CRUD operations and custom SQL queries.
 *
 * @author Masrofy Development Team
 * @version 1.0
 */
public interface IDatabase {
   
    /**
     * Initializes the database by creating necessary tables if they do not exist.
     */
    void onCreate();

    /**
     * Inserts a new record into the specified table.
     *
     * @param table the name of the table
     * @param data the object to insert (Transaction, Category, BudgetCycle, or UserProfile)
     * @return true if the insert was successful
     */
    boolean insert(String table, Object data);

    /**
     * Updates an existing record in the specified table by ID.
     *
     * @param table the name of the table
     * @param id the ID of the record to update
     * @param data the updated object
     * @return true if the update was successful
     */
    boolean update(String table, int id, Object data);

    /**
     * Deletes a record from the specified table by ID.
     *
     * @param table the name of the table
     * @param id the ID of the record to delete
     * @return true if the deletion was successful
     */
    boolean delete(String table, int id);

    /**
     * Executes a custom SQL query and returns the results as a list of objects.
     *
     * @param sql the SQL query to execute
     * @return list of mapped objects (Transaction, Category, BudgetCycle, or UserProfile)
     */
    List<Object> query(String sql);
}