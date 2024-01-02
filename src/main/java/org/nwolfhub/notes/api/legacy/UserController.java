package org.nwolfhub.notes.api.legacy;

import org.nwolfhub.notes.database.legacy.TokenController;
import org.nwolfhub.notes.database.legacy.UserDao;
import org.nwolfhub.notes.database.legacy.model.User;
import org.nwolfhub.utils.Utils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
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

    /**
     * Logins a user with username and password provided in request body with following format:
     * username=username
     * password=password
     * Possible returns:
     * 200 {"token": "token"} in case of successful authentication
     * 400 {"error": "Username or password is empty"} if no username and password were found in body
     * 401 {"error": "error"} in all other cases
     * @return see description
     */
    @PostMapping("/login")
    public static ResponseEntity<String> login(@RequestBody String body) {
        HashMap<String, String> bodyValues = Utils.parseValues(body, "\n");
        String username = bodyValues.get("username");
        String password = bodyValues.get("password");
        if (username==null || password==null) {
            return ResponseEntity.status(400).body(JsonBuilder.buildFailOutput("Username or password is empty"));
        }
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
            return ResponseEntity.status(401).body(JsonBuilder.buildFailOutput("Passwords didnt match"));
        } else {
            return ResponseEntity.status(401).body(JsonBuilder.buildFailOutput("User " + username + " does not exist"));
        }
    }

    /**
     * registers a user with username and password provided in body with following format:
     * username=username
     * password=password
     * Possible returns:
     * 200 {"token": "token"} in case of successful authentication
     * 400 {"error": "Username or password is empty"} if no username and password were found in body
     * 401 {"error": "error"} in all other cases
     * @return see description
     */
    @PostMapping("/register")
    public static ResponseEntity<String> register(@RequestBody String body) {
        HashMap<String, String> bodyValues = Utils.parseValues(body, "\n");
        String username = bodyValues.get("username");
        String password = bodyValues.get("password");
        if (username==null || password==null) {
            return ResponseEntity.status(400).body(JsonBuilder.buildFailOutput("Username or password is empty"));
        }
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


    @PostMapping("/changePassword")
    public static ResponseEntity<String> changePassword(@RequestBody String body) {
        HashMap<String, String> bodyValues = Utils.parseValues(body, "\n");
        String username = bodyValues.get("username");
        String password = bodyValues.get("password");
        String newPassword = bodyValues.get("newPassword");
        if (username==null || password==null || newPassword==null) {
            return ResponseEntity.status(400).body(JsonBuilder.buildFailOutput("Username or password is empty"));
        }
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
                return ResponseEntity.status(401).body(JsonBuilder.buildFailOutput("Previous passwords didnt match"));
            }
        } else {
            return ResponseEntity.status(400).body(JsonBuilder.buildFailOutput("User " + username + " does not exits"));
        }
    }

    @PostMapping("/updateUser")
    public static ResponseEntity<String> updateUser() {
        return ResponseEntity.status(501).body(JsonBuilder.buildFailOutput("Method not implemented yet"));
    }

    @GetMapping("/getMe")
    public static ResponseEntity<String> getMe(@RequestHeader(name = "token") String token) {
        User user = TokenController.getUser(token);
        if(user!=null) {
            return ResponseEntity.status(200).body(JsonBuilder.buildGetMe(user));
        } else {
            return ResponseEntity.status(401).body(JsonBuilder.buildFailOutput("Token verification failed"));
        }
    }

    @GetMapping("/checkAuth")
    public static ResponseEntity<String> checkAuth(@RequestHeader(name="token") String token) {
        if(TokenController.getUserId(token)!=null) {
            return ResponseEntity.status(200).body(JsonBuilder.buildOk());
        } else {
            return ResponseEntity.status(401).body(JsonBuilder.buildFailOutput("Token verification failed"));
        }
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public static ResponseEntity<String> noParameter(MissingServletRequestParameterException e) {
        return ResponseEntity.status(400).body(JsonBuilder.buildFailOutput("One or more required parameters were not provided: " + e));
    }


    //inner methods
    public static User getUser(Integer id) {
        return dao.getUser(id);
    }
    public static User getUser(String username) {
        return dao.getUser(username);
    }
}
