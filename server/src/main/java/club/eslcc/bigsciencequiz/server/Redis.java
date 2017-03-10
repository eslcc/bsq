package club.eslcc.bigsciencequiz.server;

import redis.clients.jedis.Jedis;

import java.util.Optional;

/**
 * Created by marks on 10/03/2017.
 */
public class Redis {
    private static Jedis jedis;
    static {
        jedis = new Jedis(Optional.ofNullable(System.getenv("REDIS_HOST")).orElse("localhost"), 6379);
    }

    public static Jedis getJedis() {
        return jedis;
    }
}
