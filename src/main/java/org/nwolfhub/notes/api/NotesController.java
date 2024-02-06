package org.nwolfhub.notes.api;

import org.aspectj.weaver.ast.Not;
import org.nwolfhub.notes.database.model.Note;
import org.nwolfhub.notes.database.model.PublicShare;
import org.nwolfhub.notes.database.model.User;
import org.nwolfhub.notes.database.repositories.NoteRepository;
import org.nwolfhub.notes.database.repositories.ShareRepository;
import org.nwolfhub.notes.database.repositories.UserRepository;
import org.nwolfhub.utils.Utils;
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
    public Integer maxNotes;
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
    @GetMapping("/{note}/get")
    public ResponseEntity<String> getNote(@AuthenticationPrincipal Jwt jwt, @PathVariable(name = "note") String noteId) {
        Optional<Note> requestedNote = noteRepository.findNoteByIdAndOwner(noteId, new User().setId(jwt.getSubject()));
        if(requestedNote.isPresent()) {
            Note note = requestedNote.get();
            return ResponseEntity.ok(JsonBuilder.buildNote(note));
        } else return ResponseEntity.notFound().build();
    }

    @GetMapping("/{share-id}/getShared")
    public ResponseEntity<String> getSharedNote(@AuthenticationPrincipal Jwt jwt, @PathVariable(name = "share-id") String shareId) {
        Optional<PublicShare> requestedPublicShare = shareRepository.findById(shareId);
        if(requestedPublicShare.isPresent()) {
            PublicShare publicShare = requestedPublicShare.get();
            if(publicShare.getTo().getId().equals(jwt.getSubject())) {
                Note note = publicShare.getNote();
                return ResponseEntity.ok(JsonBuilder.buildNote(note.setId(shareId)));
            } else {
                return ResponseEntity.notFound().build();
            }
        } else return ResponseEntity.notFound().build();
    }

    @PostMapping("/{share-id}/editShared")
    public ResponseEntity<String> editSharedNote(@AuthenticationPrincipal Jwt jwt, @PathVariable(name = "share-id") String shareId, @RequestBody String content) {
        Optional<PublicShare> requestedPublicShare = shareRepository.findById(shareId);
        if(requestedPublicShare.isPresent()) {
            PublicShare publicShare = requestedPublicShare.get();
            if(publicShare.getTo().getId().equals(jwt.getSubject())) {
                if(publicShare.getPermission()>=1) {
                    Note note = publicShare.getNote().setContent(content);
                    noteRepository.save(note);
                    return ResponseEntity.ok(JsonBuilder.buildOk());
                } else return ResponseEntity.status(HttpStatus.FORBIDDEN).body(JsonBuilder.buildErr("You cannot edit this note"));
            } else return ResponseEntity.notFound().build();
        } else return ResponseEntity.notFound().build();
    }

    @PostMapping("/{note}/edit")
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

    @PostMapping("/create")
    public ResponseEntity<String> createNote(@AuthenticationPrincipal Jwt jwt, @RequestParam(name = "name") String name, @RequestBody String content) {
        Integer currentAmount = noteRepository.countByOwner(new User().setId(jwt.getSubject()));
        if(currentAmount<maxNotes) {
            String noteId = Utils.generateString(200);
            while (noteRepository.findNoteById(noteId).isPresent()) {
                noteId = Utils.generateString(210);
            }
            Note note = new Note().setId(noteId).setContent(content).setName(name).setOwner(new User().setId(jwt.getSubject()));
            noteRepository.save(note);
            return ResponseEntity.ok(JsonBuilder.buildNoteCreateOk(noteId));
        } else return ResponseEntity.badRequest().body(JsonBuilder.buildErr("You have hit the notes limit on this server"));
    }
    @GetMapping("/{note}/share")
    public ResponseEntity<String> shareNote(@AuthenticationPrincipal Jwt jwt, @PathVariable(name = "note") String id, @RequestParam(name = "user") String user, @RequestParam(name = "permission") Integer permission) {
        if(permission>2 || permission<0) return ResponseEntity.badRequest().body(JsonBuilder.buildErr("Incorrect permission level. Must be [0,2]"));
        Optional<Note> requestedNote = noteRepository.findNoteByIdAndOwner(id, new User().setId(jwt.getSubject()));
        if(requestedNote.isPresent()) {
            Optional<User> targetUser = userRepository.findById(user);
            if(targetUser.isEmpty()) return ResponseEntity.badRequest().body(JsonBuilder.buildErr("Target user not found"));
            Note note = requestedNote.get();
            PublicShare share = new PublicShare();
            String shareId = Utils.generateString(210);
            while (shareRepository.findById(shareId).isPresent()) {
                shareId = Utils.generateString(230);
            }
            share.setId(shareId);
            share.setNote(note);
            share.setPermission(permission);
            share.setTo(targetUser.get());
            shareRepository.save(share);
            return ResponseEntity.ok(JsonBuilder.buildOk());
        } else return ResponseEntity.notFound().build();
    }
    public ResponseEntity<String> getSharedNotes() {
        return null; //tbd
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, UnsatisfiedServletRequestParameterException.class})
    public ResponseEntity<String> handleMissingParams(MissingServletRequestParameterException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

}
