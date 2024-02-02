package org.nwolfhub.notes.database.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(schema = "notes_updated", name="notes")
public class Note {
    @Id
    public String id;
    public String name;
    @OneToOne
    @JoinColumn(name = "id")
    public User owner;
    public String content;
    @JoinTable(schema = "notes_updated", name = "sharings", joinColumns = @JoinColumn(name = "id"), inverseJoinColumns = @JoinColumn(name = "note-id", referencedColumnName = "id"))
    @ManyToMany
    public List<PublicShare> sharing;

    public String getId() {
        return id;
    }

    public Note setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public Note setName(String name) {
        this.name = name;
        return this;
    }

    public User getOwner() {
        return owner;
    }

    public Note setOwner(User owner) {
        this.owner = owner;
        return this;
    }

    public String getContent() {
        return content;
    }

    public Note setContent(String content) {
        this.content = content;
        return this;
    }
}