package org.nwolfhub.notes.api;

import org.nwolfhub.notes.database.TokenController;
import org.nwolfhub.notes.database.UserDao;
import org.nwolfhub.notes.model.User;
import org.nwolfhub.utils.Utils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private static UserDao dao;

    public static void init(UserDao dao) {
        UserController.dao = dao;
    }

    @PostMapping("/login")
    public static ResponseEntity<String> login(@RequestBody String body) {
        HashMap<String, String> bodyValues = Utils.parseValues(body, "\n");
        String username = bodyValues.get("username");
        String password = bodyValues.get("password");
        User requested = dao.getUser(username);
        if(requested!=null) {
            if(requested.validatePassword(password)) {
                if(!requested.isBanned()) {
                    String token = TokenController.generateToken(requested.id);
                    return ResponseEntity.status(200).body(JsonBuilder.buildOkLoginOutput(token));
                } else {
                    return ResponseEntity.status(401).body(JsonBuilder.buildFailOutput("User is banned"));
                }

            }
            return ResponseEntity.status(401).body(JsonBuilder.buildFailOutput("Passwords didn't match"));
        } else {
            return ResponseEntity.status(401).body(JsonBuilder.buildFailOutput("User " + username + " does not exist"));
        }
    }

    @PostMapping("/register")
    public static ResponseEntity<String> register(@RequestBody String body) {
        HashMap<String, String> bodyValues = Utils.parseValues(body, "\n");
        String username = bodyValues.get("username");
        String password = bodyValues.get("password");
        User requested = dao.getUser(username);
        if(requested==null) {
            try {
                User newUser = new User(username, password);
                dao.setObject(newUser);
                newUser = dao.getUser(username); //updating user to obtain id
                String token = TokenController.generateToken(newUser.id);
                return ResponseEntity.status(200).body(JsonBuilder.buildOkLoginOutput(token));
            } catch (NoSuchAlgorithmException e) {
                return ResponseEntity.status(500).body(JsonBuilder.buildFailOutput("Could not find hashing algorithm. Please contact administrator"));
            }
        } else return ResponseEntity.status(400).body(JsonBuilder.buildFailOutput("User " + username + " already exists"));
    }

    @PostMapping("/changepassword")
    public static ResponseEntity<String> changePassword(@RequestBody String body) {
        HashMap<String, String> bodyValues = Utils.parseValues(body, "\n");
        String username = bodyValues.get("username");
        String password = bodyValues.get("password");
        String newPassword = bodyValues.get("newPassword");
        User requested = dao.getUser(username);
        if(requested!=null) {
            if(requested.validatePassword(password)) {
                try {
                    dao.setObject(requested.setPassword(newPassword));
                    return ResponseEntity.status(200).body(JsonBuilder.buildOk());
                } catch (NoSuchAlgorithmException e) {
                    return ResponseEntity.status(500).body(JsonBuilder.buildFailOutput("Could not find hashing algorithm. Please contact administrator"));
                }
            } else {
                return ResponseEntity.status(401).body(JsonBuilder.buildFailOutput("Previous passwords didn't match"));
            }
        } else {
            return ResponseEntity.status(400).body(JsonBuilder.buildFailOutput("User " + username + " does not exits"));
        }
    }

    @PostMapping("/updateuser")
    public static ResponseEntity<String> updateUser() {
        return ResponseEntity.status(501).body(JsonBuilder.buildFailOutput("Method not implemented yet"));
    }




    //inner methods
    public static User getUser(Integer id) {
        return dao.getUser(id);
    }
    public static User getUser(String username) {
        return dao.getUser(username);
    }
}
