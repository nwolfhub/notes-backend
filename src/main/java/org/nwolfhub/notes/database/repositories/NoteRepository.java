package org.nwolfhub.notes.database.repositories;

import org.nwolfhub.notes.database.model.Note;
import org.nwolfhub.notes.database.model.PublicShare;
import org.nwolfhub.notes.database.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, String> {
    List<Note> getNotesByOwner(User owner);
}
