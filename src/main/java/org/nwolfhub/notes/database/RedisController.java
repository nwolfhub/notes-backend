package org.nwolfhub.notes.database;

import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.args.FlushMode;

import java.time.Duration;

public class RedisController {
    private JedisPool pool;

    private String username; private String password;

    public RedisController(String url, Integer port, String username, String password) {
        this.username = username;
        this.password = password;
        initPool(url, port);
    }

    public void set(String key, String value) {
        try(Jedis jedis = obtainInstance()) {
            jedis.set(key, value);
        }
    }

    public String get (String key) {
        try(Jedis jedis = obtainInstance()) {
            return jedis.get(key);
        }
    }
    public void cleanup() {
        try(Jedis jedis = obtainInstance()) {
            jedis.flushDB(FlushMode.ASYNC);
        }
    }

    private JedisPoolConfig buildPoolConfig() {
        final JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(128);
        poolConfig.setMaxIdle(128);
        poolConfig.setMinIdle(16);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setMinEvictableIdleTimeMillis(Duration.ofSeconds(60).toMillis());
        poolConfig.setTimeBetweenEvictionRunsMillis(Duration.ofSeconds(30).toMillis());
        poolConfig.setNumTestsPerEvictionRun(3);
        poolConfig.setBlockWhenExhausted(true);
        return poolConfig;
    }

    private void initPool(String url, Integer port) {
        pool=new JedisPool(buildPoolConfig(), url, port);
    }

    private Jedis obtainInstance() {
        Jedis jedis = pool.getResource();
        jedis.auth(username, password);
        return jedis;
    }
}
