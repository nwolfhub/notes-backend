package org.nwolfhub.notes.model;

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

    public Note() {

    }
    public Note(User u) {
        this.owner = u;
    }
    public Note(User u, String text) {
        this.owner = u; this.text = text;
    }
    public String getNote() {
        if (encryptionType==0 || encryptionType>=2) {
            return text;
        } if (encryptionType==1) {
            throw new NoAuthException("Encryption type 1 expects a call with a password provided");
        }
        throw new IllegalStateException("Encryption type didn't match any expected value");
    }

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
}
