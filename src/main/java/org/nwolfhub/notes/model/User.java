package org.nwolfhub.notes.model;

import jakarta.persistence.*;
import org.nwolfhub.utils.Utils;

import java.io.Serializable;
import java.security.NoSuchAlgorithmException;

@Entity
@Table(name = "users", schema = "notes")
public class User implements Serializable {
    @Id
    @SequenceGenerator(name = "uidGen", sequenceName = "users.id_increaser", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "uidGen")
    public Integer id;

    public String username;
    public String name;
    public String salt1;
    public String salt2;
    public String password;

    public boolean isBanned;

    public User() {

    }

    public User(String username, String password) throws NoSuchAlgorithmException {
        this.username = username;
        this.name = username;
        this.salt1 = Utils.generateString(40);
        this.salt2 = Utils.generateString(20);
        this.password = Utils.hashString(salt1 + password + salt2);
    }

    public Integer getId() {
        return id;
    }

    public User setId(Integer id) {
        this.id = id;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public User setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getName() {
        return name;
    }

    public User setName(String name) {
        this.name = name;
        return this;
    }

    public String getSalt1() {
        return salt1;
    }

    public User setSalt1(String salt1) {
        this.salt1 = salt1;
        return this;
    }

    public String getSalt2() {
        return salt2;
    }

    public User setSalt2(String salt2) {
        this.salt2 = salt2;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public User setPassword(String password) {
        this.password = password;
        return this;
    }

    public boolean isBanned() {
        return isBanned;
    }

    public User setBanned(boolean banned) {
        isBanned = banned;
        return this;
    }
}
