package org.nwolfhub.notes.database.model;

import jakarta.persistence.*;

import java.util.List;

/**
 * A note shared to other users
 */
@Entity
@Table(schema = "notes_updated", name = "share")
public class PublicShare {
    @Id
    public String id;
    public Integer permission; //0 - read, 1-edit, 2-delete, reshare (with 1 or less)
    @ManyToOne
    public Note note;

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

    public User getTo() {
        return to;
    }

    public PublicShare setTo(User to) {
        this.to = to;
        return this;
    }

    public Note getNote() {
        return note;
    }

    public PublicShare setNote(Note note) {
        this.note = note;
        return this;
    }
}
