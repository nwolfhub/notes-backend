package org.nwolfhub.notes.model;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "users", schema = "notes")
public class User implements Serializable {
    @Id
    @SequenceGenerator(name = "uidGen", sequenceName = "users.id_increaser", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "uidGen")
    public Integer id;

    public String username;
    public String name;
    public String hash1;
    public String hash2;
    public String password;

    public boolean isBanned;

    public User() {

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

    public String getHash1() {
        return hash1;
    }

    public User setHash1(String hash1) {
        this.hash1 = hash1;
        return this;
    }

    public String getHash2() {
        return hash2;
    }

    public User setHash2(String hash2) {
        this.hash2 = hash2;
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
