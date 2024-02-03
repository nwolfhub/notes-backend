package org.nwolfhub.notes.api;

import org.nwolfhub.notes.database.model.Note;
import org.nwolfhub.notes.database.model.User;
import org.nwolfhub.notes.database.repositories.NoteRepository;
import org.nwolfhub.notes.database.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.UnsatisfiedServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class NotesController {
    @Value("${users.max-notes}")
    public String maxNotes;
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    public NotesController(NoteRepository repository, UserRepository userRepository) {
        this.noteRepository = repository;
        this.userRepository = userRepository;
    }

    public ResponseEntity<String> getNotes(@AuthenticationPrincipal Jwt jwt) {
        User owner = new User().setId(jwt.getSubject());
        List<Note> notes = noteRepository.getNotesByOwner(owner);
        return ResponseEntity.ok(JsonBuilder.buildNotesList(notes));
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, UnsatisfiedServletRequestParameterException.class})
    public ResponseEntity<String> handleMissingParams(MissingServletRequestParameterException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

}
