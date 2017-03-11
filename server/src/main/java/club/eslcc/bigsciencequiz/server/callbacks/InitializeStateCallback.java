package club.eslcc.bigsciencequiz.server.callbacks;

import static club.eslcc.bigsciencequiz.proto.Gamestate.*;

import club.eslcc.bigsciencequiz.proto.QuestionOuterClass;
import club.eslcc.bigsciencequiz.server.IStartupCallback;
import club.eslcc.bigsciencequiz.server.Redis;
import club.eslcc.bigsciencequiz.server.Server;
import redis.clients.jedis.Jedis;

import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by marks on 11/03/2017.
 */
public class InitializeStateCallback implements IStartupCallback {
    private static Jedis jedis = Redis.getJedis();
    private static Logger log = Logger.getGlobal();

    @Override
    public void onStartup() {
        if (jedis.exists("state")) {
            log.log(Level.INFO, "[InitializeStateCallback] Skipping state initialization because state exists");

            if (!Server.PROD) {
                jedis.del("devices");
                jedis.del("team_names");
            }
            return;
        }
        jedis.hset("state", "state", GameState.State.NOTREADY.toString());
        try {
            jedis.hset("state".getBytes("UTF-8"), "currentQuestion".getBytes("UTF-8"), QuestionOuterClass.Question.newBuilder().build().toByteArray());
        } catch (UnsupportedEncodingException e) {
            // Can't happen
            throw new RuntimeException(e);
        }
    }
}
