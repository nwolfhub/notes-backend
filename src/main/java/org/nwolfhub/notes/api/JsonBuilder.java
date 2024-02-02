package org.nwolfhub.notes.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.nwolfhub.notes.database.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JsonBuilder {
    @Value("${server.name}")
    static String serverName;
    public static String serverInfo = "{\"api_version\": \"1\", \"name\": \"" + serverName + "\"}";

    public static String buildIndirectLogin(String url) {
        return "{\"indirect\": 1, \"url\": \"" + url + "\"}";
    }

    public static String buildOk() {
        return "{\"ok\": 1}";
    }

    public static String buildSearchResults(List<User> users) {
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
}
