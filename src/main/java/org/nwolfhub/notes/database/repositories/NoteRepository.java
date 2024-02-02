package org.nwolfhub.notes.database.repositories;

import org.nwolfhub.notes.database.model.Note;
import org.nwolfhub.notes.database.model.PublicShare;
import org.nwolfhub.notes.database.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface NoteRepository extends JpaRepository<Note, String> {
    List<Note> getNotesByOwner(User owner);
}
