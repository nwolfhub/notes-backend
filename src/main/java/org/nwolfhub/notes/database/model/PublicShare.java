package org.nwolfhub.notes.database.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class PublicShare {
    @Id
    public String id;
    @OneToOne
    @JoinColumn(name = "id")
    public User sharedTo;

    public Integer permission;
}
