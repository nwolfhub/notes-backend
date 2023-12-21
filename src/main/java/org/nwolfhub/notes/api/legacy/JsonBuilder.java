package org.nwolfhub.notes.api.legacy;

import org.nwolfhub.notes.model.Note;
import org.nwolfhub.notes.model.User;

import java.util.List;

public class JsonBuilder {
    public static String buildOkLoginOutput(String token) {
        return "{\"token\": \"" + token + "\"}";
    }
    public static String buildFailOutput(String error) {
        return "{\"error\": \"" + error + "\"}";
    }

    public static String buildOk() {
        return "{\"ok\": 1\"}";
    }

    public static String buildGetMe(User u) {
        return "{\"user\": \"" + u.username + "\", \"id\": " + u.id + "}";
    }

    public static String buildGetNote(String note) {
        return "{\"note\": \"" + note.replace("\"", "\\\"") + "\"}";
    }

    public static String buildGetNotes(List<Note> notes) {
        StringBuilder builder = new StringBuilder();
        builder.append("{\"notes\": [");
        boolean first = true;
        for(Note note:notes) {
            if(!first) {
                builder.append(",");
            }
            builder.append("{");
            builder.append("\"name\": \"").append(note.getName()).append("\",");
            builder.append("\"encryption\": ").append(note.getEncryptionType());
            builder.append("}");
            first=false;
        }
        builder.append("]}");
        return builder.toString();
    }

    public static String buildPrivilegesList(List<String> privileges) {
        StringBuilder builder = new StringBuilder("{\"privileges\": [");
        for(String privilege:privileges) {
            builder.append("\"").append(privilege).append("\"");
        }
        builder.append("]}");
        return builder.toString();
    }

    public static String buildDonationServerUrlResponse(String server) {
        return ("{\"url\": \"" + server + "\"}");
    }
}
