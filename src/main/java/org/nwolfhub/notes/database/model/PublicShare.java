package org.nwolfhub.notes.database.model;

import jakarta.persistence.*;

@Entity
@Table(schema = "notes_updated", name = "share")
public class PublicShare {
    @Id
    public String id;
    public Integer permission;

    @ManyToOne
    public User to;

    public String getId() {
        return id;
    }

    public PublicShare setId(String id) {
        this.id = id;
        return this;
    }

    public Integer getPermission() {
        return permission;
    }

    public PublicShare setPermission(Integer permission) {
        this.permission = permission;
        return this;
    }
}
