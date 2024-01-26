package org.nwolfhub.notes.database.model;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import org.jetbrains.annotations.NotNull;

@Entity
@Table(schema = "notes_updated", name = "users")
public class User {
    @Id
    @SequenceGenerator(name = "appsGen", sequenceName = "notes.userid_increaser", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "appsGen")
    public Integer id;
    public String keycloakId;
    public String username;
    public String displayName;


    public Integer getId() {
        return id;
    }

    public User setId(Integer id) {
        this.id = id;
        return this;
    }

    public String getKeycloakId() {
        return keycloakId;
    }

    public User setKeycloakId(String keycloakId) {
        this.keycloakId = keycloakId;
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
