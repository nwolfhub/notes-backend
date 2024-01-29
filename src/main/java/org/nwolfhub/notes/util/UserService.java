package org.nwolfhub.notes.util;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.nwolfhub.notes.database.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    String issuer;

    public User registerUserInDb(Jwt jwt) {
        OkHttpClient client = new OkHttpClient();

        client.newCall(new Request.Builder().url(issuer + "/protocol/openid-connect/userinfo").post(
                RequestBody.create("")
        ))
    }
}
