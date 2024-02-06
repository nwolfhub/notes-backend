package org.nwolfhub.notes.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;
import org.nwolfhub.notes.database.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;

@Service
public class UserService {
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    String issuer;

    public User obtainUserInfoFromKeycloak(Jwt jwt) {
        OkHttpClient client = new OkHttpClient();

        try {
            Response response = client.newCall(new Request.Builder()
                    .url(issuer + "/protocol/openid-connect/userinfo")
                    .get()
                    .addHeader("Authorization", "Bearer " + jwt.getTokenValue())
                    .build()).execute();
            if(response.isSuccessful()) {
                User user = new User();
                JsonObject object = JsonParser.parseString(response.body().string()).getAsJsonObject();
                user.setUsername(object.get("preferred_username").getAsString());
                if(object.has("given_name")) user.setDisplayName(object.get("given_name").getAsString());
                user.setId(jwt.getSubject());
                return user;
            } else throw new IOException("Failed to contact keycloak: " + (response.body()!=null?response.body().string():response.code()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
