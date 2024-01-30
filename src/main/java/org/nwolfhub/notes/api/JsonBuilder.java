package org.nwolfhub.notes.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JsonBuilder {
    @Value("${server.name}")
    static String serverName;
    public static String serverInfo = "{\"api_version\": \"1\", \"name\": \"" + serverName + "\"}";

    public static String buildIndirectLogin(String url) {
        return "{\"indirect\": 1, \"url\": \"" + url + "\"}";
    }
}
