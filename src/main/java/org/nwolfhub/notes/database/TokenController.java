package org.nwolfhub.notes.database;

import org.nwolfhub.notes.Configurator;
import org.nwolfhub.notes.api.UserController;
import org.nwolfhub.notes.model.User;
import org.nwolfhub.utils.Utils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.args.FlushMode;

import java.util.Date;
import java.util.HashMap;
import java.util.Set;

public class TokenController {
    private static RedisController controller;
    private static HashMap<String, Integer> tokenToUser;
    private static Long lastCleanup;
    private static Integer cleanupRate = 24;

    public static void init() {
        String useRedis = Configurator.getEntry("use_redis");
        if (useRedis!=null && useRedis.equals("true")) {
            Jedis jedis = new Jedis(Configurator.getEntry("redis_url"), Integer.parseInt(Configurator.getEntry("redis_port")));
            jedis.auth(Configurator.getEntry("redis_user"), Configurator.getEntry("redis_password"));
            jedis.select(Integer.parseInt(Configurator.getEntry("redis_db_id")));
            jedis.flushDB(FlushMode.SYNC);
            lastCleanup = new Date().getTime();
            String rate = Configurator.getEntry("cleanup_rate");
            if(rate==null) {
                cleanupRate = 24;
            } else {
                cleanupRate = Integer.valueOf(rate);
            }
            controller = new RedisController(Configurator.getEntry("redis_url"), Integer.parseInt(Configurator.getEntry("redis_port")), Configurator.getEntry("redis_user"), Configurator.getEntry("redis_password"));
        } else {
            tokenToUser = new HashMap<>();
            String rate = Configurator.getEntry("cleanup_rate");
            if(rate==null) {
                cleanupRate = 24;
            } else {
                cleanupRate = Integer.valueOf(rate);
            }
        }
        new Thread(TokenController::cleanUp).start();
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

    public static String generateToken(Integer user) {
        String token = Utils.generateString(130);
        if(controller==null) {
            tokenToUser.put(token, user);
        } else {
            controller.set(token, user.toString());
        }
        return token;
    }

    private static void cleanUp() {
        Date date = new Date();
        while (true) {
            if(date.getTime() - cleanupRate*3600000>=lastCleanup) {
                if(controller!=null) {
                    controller.cleanup();
                } else {
                    tokenToUser.clear();
                }
                lastCleanup = date.getTime();
            }
            try {
                Thread.sleep(60000);
            } catch (InterruptedException ignored) {}
        }
    }
}
