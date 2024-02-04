package org.nwolfhub.notes.api;

import org.nwolfhub.notes.database.model.Note;
import org.nwolfhub.notes.database.model.PublicShare;
import org.nwolfhub.notes.database.model.User;
import org.nwolfhub.notes.database.repositories.NoteRepository;
import org.nwolfhub.notes.database.repositories.ShareRepository;
import org.nwolfhub.notes.database.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.UnsatisfiedServletRequestParameterException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/notes")
public class NotesController {
    @Value("${users.max-notes}")
    public String maxNotes;
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final ShareRepository shareRepository;

    public NotesController(NoteRepository repository, UserRepository userRepository, ShareRepository shareRepository) {
        this.noteRepository = repository;
        this.userRepository = userRepository;
        this.shareRepository = shareRepository;
    }

    @GetMapping("/getNotes")
    public ResponseEntity<String> getNotes(@AuthenticationPrincipal Jwt jwt) {
        User owner = new User().setId(jwt.getSubject());
        List<Note> notes = noteRepository.getNotesByOwner(owner);
        return ResponseEntity.ok(JsonBuilder.buildNotesList(notes));
    }
    @GetMapping("/note/{note}/get")
    public ResponseEntity<String> getNote(@AuthenticationPrincipal Jwt jwt, @PathVariable(name = "note") String noteId) {
        Optional<Note> requestedNote = noteRepository.findNoteByIdAndOwner(noteId, new User().setId(jwt.getSubject()));
        if(requestedNote.isPresent()) {
            Note note = requestedNote.get();
            return ResponseEntity.ok(JsonBuilder.buildNote(note));
        } else return ResponseEntity.notFound().build();
    }

    @GetMapping("/note/{share-id}/getShared")
    public ResponseEntity<String> getSharedNote(@AuthenticationPrincipal Jwt jwt, @PathVariable(name = "share-id") String shareId) {
        Optional<PublicShare> requestedPublicShare = shareRepository.findById(shareId);
        if(requestedPublicShare.isPresent()) {
            PublicShare publicShare = requestedPublicShare.get();
            if(publicShare.getTo().getId().equals(jwt.getSubject())) {
                Optional<Note> associatedNote = noteRepository.findNoteById(publicShare.get());
                if(associatedNote.isPresent()) {
                    Note note = associatedNote.get();
                    return ResponseEntity.ok(JsonBuilder.buildNote(note.setId(shareId)));
                } else {
                    new Thread(() -> {

                    }).start();
                    return ResponseEntity.status(HttpStatus.GONE).body(JsonBuilder.buildErr("The original note was deleted." +
                            "Executing further requests with same share-id will result into 404"));
                }
            }
        }
    }

    @PostMapping("/note/{note}/edit")
    public ResponseEntity<String> editNote(@AuthenticationPrincipal Jwt jwt, @PathVariable(name = "note") String note, @RequestBody String content) {
        Optional<Note> requested = noteRepository.findNoteByIdAndOwner(note, new User().setId(jwt.getSubject()));
        if(requested.isPresent()) {
            Note resultedNote = requested.get();
            resultedNote.setContent(content);
            noteRepository.save(resultedNote);
            return ResponseEntity.ok(JsonBuilder.buildOk());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, UnsatisfiedServletRequestParameterException.class})
    public ResponseEntity<String> handleMissingParams(MissingServletRequestParameterException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

}
