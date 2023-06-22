package org.nwolfhub.notes.database;

import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.args.FlushMode;

public class RedisController {
    private Jedis jedis;

    public RedisController(Jedis jedis, String username, String password) {
        this.jedis = jedis;
        jedis.auth(username, password);
    }

    public RedisController(Jedis jedis) {
        this.jedis = jedis;
    }

    public void set(String key, String value) {
        jedis.set(key, value);
    }

    public String get (String key) {
        return jedis.get(key);
    }
    public void cleanup() {
        jedis.flushDB(FlushMode.ASYNC);
    }
}
