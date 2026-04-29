package controller;

import javafx.scene.control.Alert;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
 
public class AuthManager {
    private String storedHash;
 
    public AuthManager(String storedHash) {
        this.storedHash = storedHash;
    }
 
    public boolean authenticate(int pin) {
        return hash(pin).equals(this.storedHash);
    }

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
 
    public void changePIN(int newPin) {
        this.storedHash = hash(newPin);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Security Update");
        alert.setHeaderText(null);
        alert.setContentText("PIN updated successfully.");
        alert.showAndWait();
    }
 
    public String getStoredHash() {
        return storedHash;
    }
}