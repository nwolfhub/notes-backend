package org.nwolfhub.notes.api;

import org.nwolfhub.notes.database.UserDao;
import org.nwolfhub.notes.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private static UserDao dao;

    public static void init(UserDao dao) {
        UserController.dao = dao;
    }
    public static ResponseEntity<String> login(String username, String password) {
        User requested = dao.getUser(username);

    }




    //inner methods
    public static User getUser(Integer id) {
        return dao.getUser(id);
    }
    public static User getUser(String username) {
        return dao.getUser(username);
    }
}
