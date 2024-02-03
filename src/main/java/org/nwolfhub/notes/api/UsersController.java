package org.nwolfhub.notes.api;


import org.nwolfhub.notes.database.model.User;
import org.nwolfhub.notes.database.repositories.UserRepository;
import org.nwolfhub.notes.util.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/users")
public class UsersController {
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    String issuer;
    private final UserService service;
    public ResponseEntity<String> getLoginData() {
        return ResponseEntity.ok("{\"url\": \"" + issuer + "/protocol/openid-connect/auth?\",");
    }
    private final Logger logger = LoggerFactory.getLogger(UsersController.class);

    private final UserRepository repository;

    public UsersController(UserRepository repository, UserService service) {
        this.repository = repository;
        this.service = service;
    }

    @GetMapping("/postLogin")
    public ResponseEntity<String> postLogin(@AuthenticationPrincipal Jwt jwt) {
        Optional<User> user = repository.findById(jwt.getSubject());
        if(user.isPresent()) return ResponseEntity.ok(JsonBuilder.buildOk());else {
            logger.info("New user found. Registering in database: " + jwt.getSubject());
            new Thread(() -> {
                User user1 = service.obtainUserInfoFromKeycloak(jwt);
                repository.saveAndFlush(user1);
            }).start();
            return ResponseEntity.accepted().body(JsonBuilder.buildOk());
        }
    }

    @GetMapping("/searchUsers")
    public ResponseEntity<String> searchUsers(@AuthenticationPrincipal Jwt jwt, @RequestParam(name = "username") String username) {
        logger.trace(jwt.getSubject() + " requested search for " + username);
        List<User> users = repository.findTop3ByUsernameLikeIgnoreCase(username);
        return ResponseEntity.ok(JsonBuilder.buildSearchResults(users));
    }

    @GetMapping("/getMe")
    public ResponseEntity<String> getMe(@AuthenticationPrincipal Jwt jwt) {
        Optional<User> user = repository.findById(jwt.getSubject());
        return user.map(value -> ResponseEntity.ok(JsonBuilder.buildMe(value))).orElseGet(() -> ResponseEntity.badRequest().body(JsonBuilder.buildErr("User not found in database. Did you execute postLogin?")));
    }
    
}
