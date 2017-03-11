package club.eslcc.bigsciencequiz.server;

import redis.clients.jedis.Jedis;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by marks on 10/03/2017.
 */
public class Redis {
    private static Jedis jedis;
    static {
        String host = Optional.ofNullable(System.getenv("REDIS_HOST")).orElse("localhost");
        Logger.getGlobal().log(Level.INFO, "Connecting to Redis " + host);
        jedis = new Jedis(host, 6379);
    }

    public static Jedis getJedis() {
        return jedis;
    }
}
