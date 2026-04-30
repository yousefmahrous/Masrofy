package model;

/**
 * Represents a user profile in the Masrofy application.
 * Contains the user's name and the hashed PIN for authentication.
 *
 * @author Masrofy Development Team
 * @version 1.0
 */
public class UserProfile {
    private String name;
    private String hashedPIN;

    /**
     * Constructs a new UserProfile with name and hashed PIN.
     *
     * @param name the user's name
     * @param hashedPIN the SHA-256 hash of the user's PIN
     */
    public UserProfile(String name, String hashedPIN) {
        this.name = name;
        this.hashedPIN = hashedPIN;
    }

    /**
     * Returns the user's name.
     *
     * @return the name of the user
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the hashed PIN.
     *
     * @return the hashed PIN string
     */
    public String getHashedPIN() {
        return hashedPIN;
    }

    /**
     * Sets the user's name.
     *
     * @param name the new name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the hashed PIN.
     *
     * @param hashedPIN the new hashed PIN
     */
    public void setHashedPIN(String hashedPIN) {
        this.hashedPIN = hashedPIN;
    }
}