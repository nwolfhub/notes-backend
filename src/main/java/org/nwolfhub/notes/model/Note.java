package org.nwolfhub.notes.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;

import java.io.Serializable;

/**
 * Basic note class. Contains a user and a notes text
 */
public class Note implements Serializable {
    public String name;
    public User owner;
    private String text;
    /*
    Possible encryption types:
    0 - unencrypted
    1 - basic password protection. No real encryption, just an extra check on server side
    2 - blowfish encryption. Password not stored on server side.
    3+ - custom encryption. Client can request password verification
     */
    public int encryptionType;
    private String password;

    public User getOwner() {
        return owner;
    }

    public Note() {

    }
    public Note(User u) {
        this.owner = u;
    }
    public Note(User u, String text) {
        this.owner = u; this.text = text;
    }
    /**
     * This method returns the note if encryption type is 0 or greater than or equal to 2.
     * If encryption type is 1, it throws NoAuthException with message "Encryption type 1 expects a call with a password provided".
     * If encryption type doesn't match any expected value, it throws IllegalStateException with message "Encryption type didn't match any expected value".
     * @return The note if all condition matches
     * @throws NoAuthException If encryption type is 1.
     * @throws IllegalStateException If encryption type doesn't match any expected value.
     */
    public String getNote() {
        if (encryptionType==0 || encryptionType>=2) {
            return text;
        } if (encryptionType==1) {
            throw new NoAuthException("Encryption type 1 expects a call with a password provided");
        }
        throw new IllegalStateException("Encryption type didn't match any expected value");
    }
    /**
     * This method returns the note if encryption type is 0 or greater than or equal to 2.
     * If encryption type is 1, it checks if the password matches and returns the note.
     * If password doesn't match, it throws NoAuthException.
     * If encryption type doesn't match any expected value, it throws IllegalStateException.
     * @param password The password to be checked if encryption type is 1.
     * @return The note if all conditions matches
     * @throws NoAuthException If password doesn't match when encryption type is 1.
     * @throws IllegalStateException If encryption type doesn't match any expected value.
     */

    public String getNote(String password) {
        if (encryptionType==0 || encryptionType>=2) {
            return text;
        } if (encryptionType==1) {
            if(password.equals(this.password)) {
                return text;
            }
            throw new NoAuthException("Passwords didn't match");
        }
        throw new IllegalStateException("Encryption type didn't match any expected value");
    }

    public boolean verifyPassword(String password) {
        return this.password.equals(password);
    }

    public String getName() {
        return name;
    }

    public Note setName(String name) {
        this.name = name;
        return this;
    }

    public Note setOwner(User owner) {
        this.owner = owner;
        return this;
    }

    public int getEncryptionType() {
        return encryptionType;
    }

    public Note setEncryptionType(int encryptionType) {
        this.encryptionType = encryptionType;
        return this;
    }

    public Note setText(String text) {
        this.text = text;
        return this;
    }

    public String getPassword() {
        if(encryptionType>=3) {
            return password;
        } else throw new NoAuthException("Cannot request password from encryption type less than 3");
    }

    public Note setPassword(String prevPassword, String password) {
        if(encryptionType==0 || encryptionType>=3) {
            this.password = password;
        } else if(encryptionType==1) {
            if(prevPassword.equals(password)) {
                this.password = password;
            } else throw new NoAuthException("Previous password didn't match");
        }
        return this;
    }

    public Note setPassword(String password) {
        this.password = password;
        return this;
    }
}
