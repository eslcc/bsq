package club.eslcc.bigsciencequiz.server;

import static club.eslcc.bigsciencequiz.proto.QuestionOuterClass.*;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import redis.clients.jedis.Jedis;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import static club.eslcc.bigsciencequiz.proto.Gamestate.*;

/**
 * Created by marks on 10/03/2017.
 */
public class RedisHelpers {
    private static Jedis jedis = Redis.getJedis();

    public static GameState getGameState(String userId) {
        Map<String, String> state = jedis.hgetAll("state");
        GameState.Builder builder = GameState.newBuilder();
        builder.setState(GameState.State.valueOf(state.get("state")));

        try {
            // TODO: copyFrom will be called often, perhaps cache the ByteString?
            String currentQuestion = state.get("currentQuestion");
            if (currentQuestion != null) {
                Question question = Question.parseFrom(ByteString.copyFrom(currentQuestion, "UTF-8"));
                builder.setCurrentQuestion(question);
            }

            if (userId != null) {
                builder.setMyCurrentQuestionAnswer(Integer.parseInt(jedis.hget("answers", userId)));
            }

            return builder.build();
        } catch (InvalidProtocolBufferException e) {
            // Most definitely can happen.
            throw new IllegalStateException(e);
        } catch (UnsupportedEncodingException e) {
            // Can't happen.
            throw new RuntimeException(e);
        }
    }
}
