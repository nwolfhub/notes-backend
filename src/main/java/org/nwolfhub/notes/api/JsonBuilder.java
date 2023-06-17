package org.nwolfhub.notes.api;

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
}
