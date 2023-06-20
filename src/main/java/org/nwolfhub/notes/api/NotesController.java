package org.nwolfhub.notes.api;

import org.nwolfhub.easycli.Defaults;
import org.nwolfhub.notes.Configurator;
import org.nwolfhub.notes.NotesApplication;
import org.nwolfhub.notes.database.TokenController;
import org.nwolfhub.notes.model.NoAuthException;
import org.nwolfhub.notes.model.Note;
import org.nwolfhub.notes.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.*;

@RequestMapping("/api/notes")
public class NotesController {
    private static String location;

    private static void validateDir() {
        if(location==null) {
            location = Configurator.getEntry("users_dir");
            if (location==null) {
                NotesApplication.cli.print("Please fill users_dir in config");
                System.exit(1);
            }
        } else {
            File f = new File(location);
            if (!f.exists()) {
                try {
                    f.createNewFile();
                } catch (IOException e) {
                    NotesApplication.cli.print(e);
                    System.exit(1);
                }
            }
        }
    }

    public static void init() {
        validateDir();
    }

    @GetMapping("/getNote")
    public static ResponseEntity<String> getNote(@RequestParam(name = "id") String id, @RequestHeader(name = "password", required = false, defaultValue = "") String password, @RequestHeader(name = "token") String token) {
        Integer owner = TokenController.getUserId(token);
        if(owner==null) {
            return ResponseEntity.status(401).body(JsonBuilder.buildFailOutput("Token verification failed"));
        }
        File noteFile = new File(location + "/" + owner + "/" + id + ".note");
        try(ObjectInputStream in = new ObjectInputStream(new FileInputStream(noteFile))) {
            Note note = (Note) in.readObject();
            String noteText;
            if(password!=null && !password.equals("")) {
                noteText = note.getNote(password);
            } else {
                noteText = note.getNote();
            }
            return ResponseEntity.status(200).body(JsonBuilder.buildGetNote(noteText));
        } catch (FileNotFoundException e) {
            return ResponseEntity.status(404).body(JsonBuilder.buildFailOutput("Note not found"));
        } catch (IOException e) {
            NotesApplication.cli.print("Could not read note " + id + ":", Defaults.boxedText);
            e.printStackTrace();
            return ResponseEntity.status(500).body(JsonBuilder.buildFailOutput("Error occurred while reading note"));
        } catch (ClassNotFoundException e) {
            NotesApplication.cli.print("Could not read note " + id + ":", Defaults.boxedText);
            return ResponseEntity.status(500).body(JsonBuilder.buildFailOutput("Note corrupted"));
        } catch (NoAuthException e) {
            return ResponseEntity.status(400).body(JsonBuilder.buildFailOutput("Note has a protection type 1, provide a password"));
        }
    }

    @PostMapping("/setNote")
    public static ResponseEntity<String> setNote(@RequestParam(name = "id") String id, @RequestHeader(name = "password", required = false, defaultValue = "") String password, @RequestBody() String body, @RequestHeader(name = "token") String token) {
        Integer owner = TokenController.getUserId(token);
        if(owner==null) {
            return ResponseEntity.status(401).body(JsonBuilder.buildFailOutput("Token verification failed"));
        }
        File noteFile = new File(location + "/" + owner + "/" + id + ".note");
    }
}
