package club.eslcc.bigsciencequiz.server;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by marks on 10/03/2017.
 */
public class Redis {
    private static Jedis jedis = getNewJedis();

    public static JedisPool pool = new JedisPool(new JedisPoolConfig(), getHost());

    public static Jedis getNewJedis() {
        String host = getHost();
        Logger.getGlobal().log(Level.INFO, "Connecting to Redis " + host);
        return new Jedis(host, 6379);
    }

    private static String getHost() {
        return Optional.ofNullable(System.getenv("REDIS_HOST")).orElse("localhost");
    }

    public static Jedis getJedis() {
        return jedis;
    }
}
