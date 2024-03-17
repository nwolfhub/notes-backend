package org.nwolfhub.notes.database.model;

import jakarta.persistence.*;

import java.util.Date;
import java.util.List;

@Entity
@Table(schema = "notes_updated", name="notes")
public class Note {
    @Id
    public String id;
    public String name;
    @ManyToOne
    @JoinColumn(name = "owner", referencedColumnName = "id")
    public User owner;
    @Column(length = 32768)
    public String content;
    @JoinTable(schema = "notes_updated", name = "sharings", joinColumns = @JoinColumn(name = "id"), inverseJoinColumns = @JoinColumn(name = "note-id", referencedColumnName = "id"))
    @ManyToMany
    public List<PublicShare> sharing;

    @Basic
    @Temporal(TemporalType.TIME)
    public Date created;
    @Basic
    @Temporal(TemporalType.TIME)
    public Date lastEdited;

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

    public List<PublicShare> getSharing() {
        return sharing;
    }

    public Note setSharing(List<PublicShare> sharing) {
        this.sharing = sharing;
        return this;
    }

    public Date getCreated() {
        return created;
    }

    public Note setCreated(Date created) {
        this.created = created;
        return this;
    }

    public Date getLastEdited() {
        return lastEdited;
    }

    public Note setLastEdited(Date lastEdited) {
        this.lastEdited = lastEdited;
        return this;
    }
}
