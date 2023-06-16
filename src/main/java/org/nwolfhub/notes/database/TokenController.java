package org.nwolfhub.notes.database;

import org.nwolfhub.notes.Configurator;
import org.nwolfhub.notes.api.UserController;
import org.nwolfhub.notes.model.User;
import redis.clients.jedis.Jedis;

import java.util.HashMap;

public class TokenController {
    private static RedisController controller;
    private static HashMap<String, Integer> tokenToUser;

    public static void init() {
        String useRedis = Configurator.getEntry("use_redis");
        if (useRedis!=null && useRedis.equals("true")) {
            Jedis jedis = new Jedis(Configurator.getEntry("redis_url"), Integer.valueOf(Configurator.getEntry("redis_port")));
            jedis.auth(Configurator.getEntry("redis_user"), Configurator.getEntry("redis_password"));
            controller = new RedisController(jedis);
        } else {
            tokenToUser = new HashMap<>();
        }
    }

    public static Integer getUserId(String token) {
        if(controller!=null) {
            String userId = controller.get(token);
            if(userId==null) {
                return null;
            } else {
                return Integer.valueOf(userId);
            }
        } else {
            return tokenToUser.get(token);
        }
    }
    public static User getUser(String token) {
        Integer id = getUserId(token);
        if (id==null) {
            return null;
        } else {
            return UserController.getUser(id);
        }
    }
}
