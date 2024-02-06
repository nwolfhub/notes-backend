package org.nwolfhub.notes.database.model;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import org.jetbrains.annotations.NotNull;

@Entity
@Table(schema = "notes_updated", name = "users")
public class User {
    @Id
    public String id; //should be same as keycloak id
    public String username;
    public String displayName;


    public String getId() {
        return id;
    }

    public User setId(String id) {
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

    public String getDisplayName() {
        return displayName;
    }

    public User setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }
}
