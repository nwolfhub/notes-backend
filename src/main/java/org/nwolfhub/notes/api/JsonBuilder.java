package org.nwolfhub.notes.api;

public class JsonBuilder {
    public static String buildOkLoginOutput(String token) {
        return "{\"token\": \"" + token + "\"}";
    }
}
