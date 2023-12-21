package org.nwolfhub.notes.api.legacy;

import org.nwolfhub.easycli.Defaults;
import org.nwolfhub.notes.Configurator;
import org.nwolfhub.notes.NotesApplication;
import org.nwolfhub.notes.database.TokenController;
import org.nwolfhub.notes.database.UserDao;
import org.nwolfhub.notes.model.NoAuthException;
import org.nwolfhub.notes.model.Note;
import org.nwolfhub.notes.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Objects;

@Component
@RestController
@RequestMapping("/api/notes")
public class NotesController {
    private static String location;

    private static org.nwolfhub.utils.Configurator donationConfigurator;
    public static Boolean used;

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

    private static void validateUserDir(Integer id) throws IOException {
        File userDir = new File(location + "/" + id + "/");
        if(!userDir.isDirectory()) {
            userDir.mkdirs();
        }
    }

    public static void init(org.nwolfhub.utils.Configurator configurator, UserDao dao) {
        validateDir();
        donationConfigurator = configurator;
        PaymentController.init(configurator, dao);
    }

    @GetMapping("/get")
    public static ResponseEntity<String> getNote(@RequestParam(name = "name") String id, @RequestHeader(name = "password", required = false, defaultValue = "") String password, @RequestHeader(name = "token") String token) {
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
            in.close();
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

    @GetMapping("/create")
    public static ResponseEntity<String> createNote(@RequestParam(name = "name") String id, @RequestHeader(name = "token") String token) {
        User owner = TokenController.getUser(token);
        if (owner == null) {
            return ResponseEntity.status(401).body(JsonBuilder.buildFailOutput("Token verification failed"));
        }
        if (owner.isBanned()) {
            return ResponseEntity.status(401).body(JsonBuilder.buildFailOutput("User is banned"));
        }
        try {
            validateUserDir(owner.getId());
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(JsonBuilder.buildFailOutput("Could not create user folder due to server exception"));
        }
        File noteFile = new File(location + "/" + owner.id + "/" + id + ".note");
        if (noteFile.exists()) {
            return ResponseEntity.status(400).body(JsonBuilder.buildFailOutput("Note already exists"));
        }
        File notesDir = new File(location + "/" + owner.id + "/");
        try {
            Integer filesAmount = Objects.requireNonNull(notesDir.listFiles()).length;
            Integer maxAllowedAmount = Integer.valueOf(donationConfigurator.getValue("privilege_" + owner.getPrivilege()));
            if(filesAmount<=maxAllowedAmount) {
                noteFile.createNewFile();
                try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(noteFile))) {
                    out.writeObject(new Note(owner, "").setName(id));
                }
                return ResponseEntity.status(200).body(JsonBuilder.buildOk());
            }
            return ResponseEntity.status(400).body(JsonBuilder.buildFailOutput("Reached notes limit"));
        } catch (NumberFormatException | NullPointerException e) {
            return ResponseEntity.status(500).body(JsonBuilder.buildFailOutput("The servers privileges configuration seems wrong. Please contact your server administrator"));
        } catch (IOException e) {
            return ResponseEntity.status(500).body(JsonBuilder.buildFailOutput("Failed to create note file"));
        }
    }

    @PostMapping("/set")
    public static ResponseEntity<String> setNote(@RequestParam(name = "name") String id, @RequestHeader(name = "password", required = false) String password, @RequestBody() String body, @RequestParam(name = "encryption", required = false) String encryption, @RequestHeader(name = "updatePassword", defaultValue = "", required = false) String updatePassword, @RequestHeader(name = "token") String token) {
        User owner = TokenController.getUser(token);
        if (owner == null) {
            return ResponseEntity.status(401).body(JsonBuilder.buildFailOutput("Token verification failed"));
        }
        if (owner.isBanned()) {
            return ResponseEntity.status(401).body(JsonBuilder.buildFailOutput("User is banned"));
        }
        File noteFile = new File(location + "/" + owner.id + "/" + id + ".note");
        try {
            validateUserDir(owner.id);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(JsonBuilder.buildFailOutput("Could not create user folder due to server exception"));
        }
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(noteFile))) {
            Note note = (Note) in.readObject();
            in.close();
            if (note.encryptionType == 1) {
                if (password == null) return ResponseEntity.status(400).body(JsonBuilder.buildFailOutput("Note has a protection type 1, provide a password"));
                if (!note.verifyPassword(password)) return ResponseEntity.status(400).body(JsonBuilder.buildFailOutput("Passwords didnt match"));
            }
            note.setText(body);
            if (encryption!=null) {
                int encryptionVal = Integer.parseInt(encryption);
                if (encryptionVal < 0) {
                    encryptionVal = 0;
                }
                note.setEncryptionType(encryptionVal);
            }
            if (!updatePassword.equals("")) {
                note.setPassword(password, updatePassword);
            }
            try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(noteFile))) {
                out.writeObject(note);
                out.close();
                return ResponseEntity.status(200).body(JsonBuilder.buildOk());
            } catch (IOException e) {
                return ResponseEntity.status(500).body(JsonBuilder.buildFailOutput("Failed to write note"));
            }
        }
        catch (NoAuthException e) {
            return ResponseEntity.status(400).body(JsonBuilder.buildFailOutput("Error while changing password: " + e));
        } catch (NumberFormatException e) {
            return ResponseEntity.status(400).body(JsonBuilder.buildFailOutput("Encryption must be a positive integer"));
        } catch (FileNotFoundException e) {
            try {
                File notesDir = new File(location + "/" + owner.id + "/");
                Integer filesAmount = Objects.requireNonNull(notesDir.listFiles()).length;
                Integer maxAllowedAmount = Integer.valueOf(donationConfigurator.getValue("privilege_" + owner.getPrivilege()));
                if(maxAllowedAmount>=filesAmount) {
                    Note note = new Note();
                    try {
                        if (encryption == null) {
                            encryption = "0";
                        }
                        int encryptionVal = Integer.parseInt(encryption);
                        if (encryptionVal < 0) {
                            encryptionVal = 0;
                        }
                        note.encryptionType = encryptionVal;
                    } catch (NumberFormatException ex) {
                        return ResponseEntity.status(400).body(JsonBuilder.buildFailOutput("Encryption must be a positive integer"));
                    }
                    if (password != null) {
                        note.setPassword(password);
                    }
                    if (!updatePassword.equals("")) {
                        note.setPassword(updatePassword);
                    }
                    note.setText(body).setOwner(owner).setName(id);
                    noteFile.createNewFile();
                    try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(noteFile))) {
                        out.writeObject(note);
                        out.close();
                        return ResponseEntity.status(200).body(JsonBuilder.buildOk());
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        return ResponseEntity.status(500).body(JsonBuilder.buildFailOutput("Failed to write note"));
                    }
                } else {
                    return ResponseEntity.status(400).body(JsonBuilder.buildFailOutput("Reached notes limit"));
                }
            } catch (NumberFormatException | NullPointerException ex) {
                return ResponseEntity.status(500).body(JsonBuilder.buildFailOutput("The servers privileges configuration seems wrong. Please contact your server administrator"));
            } catch (IOException ex) {
                ex.printStackTrace();
                return ResponseEntity.status(500).body(JsonBuilder.buildFailOutput("Failed to create note file"));
            }
        } catch (IOException e) {
            NotesApplication.cli.print("Could not read note " + id + ":", Defaults.boxedText);
            e.printStackTrace();
            return ResponseEntity.status(500).body(JsonBuilder.buildFailOutput("Error occurred while reading note"));
        } catch (ClassNotFoundException e) {
            NotesApplication.cli.print("Could not read note " + id + ":", Defaults.boxedText);
            return ResponseEntity.status(500).body(JsonBuilder.buildFailOutput("Note corrupted"));
        }
    }

    @GetMapping("/getAll")
    public static ResponseEntity<String> getNotes(@RequestHeader(name = "token") String token) {
        Integer owner = TokenController.getUserId(token);
        if (owner == null) {
            return ResponseEntity.status(401).body(JsonBuilder.buildFailOutput("Token verification failed"));
        }
        File notesDir = new File(location + "/" + owner + "/");
        try {
            validateUserDir(owner);
        } catch (IOException e) {
            return ResponseEntity.status(500).body(JsonBuilder.buildFailOutput("Could not create user folder due to server exception"));
        }
        ArrayList<Note> notes = new ArrayList<>();
        try {
            for (File noteFile : Objects.requireNonNull(notesDir.listFiles((file, s) -> s.matches("(^.*\\.note$)")))) {
                try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(noteFile))) {
                    Note note = (Note) in.readObject();
                    notes.add(note);
                } catch (IOException e) {
                    NotesApplication.cli.print("Could not read note:");
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    NotesApplication.cli.print("Broken note on user " + owner + ", file " + noteFile.getAbsolutePath(), Defaults.boxedText);
                }
            }
            return ResponseEntity.status(200).body(JsonBuilder.buildGetNotes(notes));
        } catch (NullPointerException impossible) { //already handled in validateUserDir
            return ResponseEntity.status(500).body(JsonBuilder.buildFailOutput("You should not really be here"));
        }
    }

    @GetMapping("/delete")
    public static ResponseEntity<String> deleteNote(@RequestHeader(name = "token") String token, @RequestParam(name = "note") String name) {
        Integer owner = TokenController.getUserId(token);
        if (owner == null) {
            return ResponseEntity.status(401).body(JsonBuilder.buildFailOutput("Token verification failed"));
        }
        try {
            validateUserDir(owner);
        } catch (IOException e) {
            return ResponseEntity.status(500).body(JsonBuilder.buildFailOutput("Could not create user folder due to server exception"));
        }
        File noteFile = new File(location + "/" + owner + "/" + name + ".note");
        if(noteFile.exists()) {
            boolean res = noteFile.delete();
            if(res) {
                return ResponseEntity.status(200).body(JsonBuilder.buildOk());
            } else {
                return ResponseEntity.status(500).body(JsonBuilder.buildFailOutput("Failed to delete note due to server error"));
            }
        } else {
            return ResponseEntity.status(400).body(JsonBuilder.buildFailOutput("Note did not exist"));
        }
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public static ResponseEntity<String> noParameter(MissingServletRequestParameterException e) {
        return ResponseEntity.status(400).body(JsonBuilder.buildFailOutput("One or more required parameters were not provided: " + e));
    }
}
