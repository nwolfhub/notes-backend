package org.nwolfhub.notes.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UsersController {
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    String issuer;
    public ResponseEntity<String> getLoginData() {
        return ResponseEntity.ok("{\"url\": \"" + issuer + "/protocol/openid-connect/auth?\",");
    }

    public ResponseEntity<String> postLogin() {

    }
}
