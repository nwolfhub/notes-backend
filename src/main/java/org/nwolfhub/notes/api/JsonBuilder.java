package org.nwolfhub.notes.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.nwolfhub.notes.database.model.Note;
import org.nwolfhub.notes.database.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * JsonBuilder automates json generation
 */
@Component
public class JsonBuilder {

    public static String serverInfo(String serverName) {
        return "{\"api_version\": \"1\", \"name\": \"" + serverName + "\"}";
    }

    @NotNull
    @Contract(pure = true)
    public static String buildIndirectLogin(String url) {
        return "{\"indirect\": 1, \"url\": \"" + url + "\"}";
    }

    @NotNull
    @Contract(pure = true)
    public static String buildOk() {
        return "{\"ok\": 1}";
    }

    public static String buildErr(String error) {
        return "{\"ok\": 0, \"error\": \"" + error + "\"}";
    }

    public static String buildSearchResults(@NotNull List<User> users) {
        JsonObject object = new JsonObject();
        JsonArray array = new JsonArray();
        for(User user:users) {
            JsonObject userObject = new JsonObject();
            userObject.addProperty("name", user.getDisplayName());
            userObject.addProperty("username", user.getUsername());
            array.add(userObject);
        }
        object.add("users", array);
        return object.toString();
    }

    public static JsonObject buildUser(@NotNull User user) {
        JsonObject object = new JsonObject();
        object.addProperty("id", user.getId());
        object.addProperty("username", user.getUsername());
        object.addProperty("name", user.getDisplayName());
        return object;
    }

    public static String buildNotesList(@NotNull List<Note> notes) {
        JsonObject object = new JsonObject();
        JsonArray array = new JsonArray();
        for(Note note:notes) {
            JsonObject noteObject = new JsonObject();
            noteObject.addProperty("id", note.getId());
            noteObject.addProperty("name", note.getName());
            noteObject.addProperty("owner", note.getOwner().getId());
            array.add(noteObject);
        }
        object.add("notes", array);
        return object.toString();
    }
    public static String buildNote(Note note) {
        JsonObject object = new JsonObject();
        object.addProperty("name", note.getName());
        object.addProperty("content", note.getContent());
        object.add("owner", buildUser(note.getOwner()));
        object.addProperty("created", note.getCreated().getTime());
        object.addProperty("edited", note.getLastEdited().getTime());
        return object.toString();
    }
    public static String buildNoteCreateOk(String id) {
        return "{\"id\": \"" + id + "\"}";
    }
}