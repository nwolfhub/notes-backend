package org.nwolfhub.notes.database.v1.model;

import jakarta.persistence.*;
import org.nwolfhub.utils.Utils;

import java.io.Serializable;
import java.security.NoSuchAlgorithmException;

@Entity
@Table(name = "users", schema = "notesl1")
public class User implements Serializable {
    @Id
    @SequenceGenerator(name = "uidGen", sequenceName = "notes.id_increaser", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "uidGen")
    public Integer id;

    public String username;
    public String name;
    public String password;
    public String email;

    public String privilege = "default";

    public boolean isBanned;

    public User() {

    }

    public User(String username, String password) throws NoSuchAlgorithmException {
        this.username = username;
        this.name = username;
        this.isBanned = false;
        this.privilege = "default";
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
    public String getPassword() {
        return password;
    }

    public boolean isBanned() {
        return isBanned;
    }

    public User setBanned(boolean banned) {
        isBanned = banned;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public User setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getPrivilege() {
        return privilege;
    }

    public User setPrivilege(String privilege) {
        this.privilege = privilege;
        return this;
    }
}
