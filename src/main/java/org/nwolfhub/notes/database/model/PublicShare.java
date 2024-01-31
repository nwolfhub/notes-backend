package org.nwolfhub.notes.database.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class PublicShare {
    @Id
    public String id;
    @OneToOne(mappedBy = "note")
    public Note sharedNote;
    @OneToOne
    @JoinColumn(name = "id")
    public User sharedTo;
}
