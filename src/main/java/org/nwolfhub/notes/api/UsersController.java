package org.nwolfhub.notes.api;

import org.nwolfhub.notes.database.model.User;
import org.nwolfhub.notes.database.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/users")
public class UsersController {
    private final Logger logger = LoggerFactory.getLogger(UsersController.class);

    private final UserRepository repository;

    public UsersController(UserRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/postLogin")
    public ResponseEntity<String> postLogin(@AuthenticationPrincipal Jwt jwt) {
        Optional<User> user = repository.findById(jwt.getSubject());
        if(user.isPresent()) return ResponseEntity.ok(JsonBuilder.buildOk());else {
            logger.info("New user found. Registering in database: " + jwt.getSubject());
            new Thread(() -> {
                //TODO: register user in db
            }).start();
            return ResponseEntity.accepted().body(JsonBuilder.buildOk());
        }
    }


}
