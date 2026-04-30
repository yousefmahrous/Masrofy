package controller;

import javafx.scene.control.Alert;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Handles user authentication using PIN-based security with SHA-256 hashing.
 * Manages PIN hashing, authentication verification, and PIN change operations.
 *
 * @author Masrofy Development Team
 * @version 1.0
 */
public class AuthManager {
    private String storedHash;

    /**
     * Constructs an AuthManager with a stored hash for authentication.
     *
     * @param storedHash the hashed PIN to verify against
     */
    public AuthManager(String storedHash) {
        this.storedHash = storedHash;
    }

    /**
     * Authenticates a user by comparing the hash of the entered PIN with the stored hash.
     *
     * @param pin the PIN entered by the user
     * @return true if the PIN is correct, false otherwise
     */
    public boolean authenticate(int pin) {
        return hash(pin).equals(this.storedHash);
    }

    /**
     * Hashes a PIN using SHA-256 algorithm and returns the hexadecimal representation.
     *
     * @param pin the PIN to hash
     * @return hexadecimal string of the SHA-256 hash
     */
    public String hash(int pin) { 
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(String.valueOf(pin).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    /**
     * Changes the stored PIN and shows a success alert.
     *
     * @param newPin the new PIN to set
     */
    public void changePIN(int newPin) {
        this.storedHash = hash(newPin);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Security Update");
        alert.setHeaderText(null);
        alert.setContentText("PIN updated successfully.");
        alert.showAndWait();
    }

    /**
     * Returns the currently stored hash.
     *
     * @return the stored PIN hash
     */
    public String getStoredHash() {
        return storedHash;
    }
}