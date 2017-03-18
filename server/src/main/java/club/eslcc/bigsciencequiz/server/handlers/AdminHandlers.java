package club.eslcc.bigsciencequiz.server.handlers;

import static club.eslcc.bigsciencequiz.proto.Gamestate.*;
import static club.eslcc.bigsciencequiz.proto.QuestionOuterClass.*;
import static club.eslcc.bigsciencequiz.proto.Rpc.*;
import static club.eslcc.bigsciencequiz.proto.admin.AdminRpc.*;
import static club.eslcc.bigsciencequiz.server.RpcHelpers.itob;
import static club.eslcc.bigsciencequiz.server.RpcHelpers.stob;

import club.eslcc.bigsciencequiz.proto.Rpc;
import club.eslcc.bigsciencequiz.proto.admin.AdminRpc;
import club.eslcc.bigsciencequiz.server.IRpcHandler;
import club.eslcc.bigsciencequiz.server.Redis;
import club.eslcc.bigsciencequiz.server.RedisHelpers;
import com.google.protobuf.InvalidProtocolBufferException;
import com.sun.javafx.collections.MappingChange;
import org.eclipse.jetty.websocket.api.Session;
import redis.clients.jedis.Jedis;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by marks on 11/03/2017.
 */
public class AdminHandlers {
    static Jedis jedis = Redis.getJedis();

    public static class GetQuestionsHandler implements IRpcHandler {
        @Override
        public RpcResponse handle(String currentUserId, RpcRequest request, Session session) {
            RpcResponse.Builder builder = RpcResponse.newBuilder();
            if (!currentUserId.equals("ADMIN")) {
                UnauthorisedRequestResponse.Builder urrB = UnauthorisedRequestResponse.newBuilder();
                urrB.setReason("Not admin");
                builder.setUnauthorisedRequestResponse(urrB);
                return builder.build();
            }
            Map<byte[], byte[]> questions = jedis.hgetAll(stob("questions"));

            List<Question> questionProtos = questions.values().stream().map(bytes -> {
                try {
                    return Question.parseFrom(bytes);
                } catch (InvalidProtocolBufferException e) {
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toList());

            builder.setAdminGetQuestionsResponse(
                    AdminGetQuestionsResponse.newBuilder().addAllQuestions(questionProtos).build()
            );
            return builder.build();
        }
    }

    public static class ActivateQuestionHandler implements IRpcHandler {
        @Override
        public RpcResponse handle(String currentUserId, RpcRequest wrapped, Session session) {
            try {
                AdminSetActiveQuestionRequest request = wrapped.getAdminSetActiveQuestionRequest();
                GameState state = RedisHelpers.getGameState(null);
                GameState.Builder newBuilder = GameState.newBuilder(state);
                Question newQuestion = Question.parseFrom(jedis.hget(
                        stob("questions"), itob(request.getQuestionId() - 1)
                ));
                newBuilder.setCurrentQuestion(newQuestion);
                GameState newState = newBuilder.build();
                jedis.hset(stob("state"), stob("currentQuestion"), newQuestion.toByteArray());
                jedis.hset("state", "state", GameState.State.QUESTION_ANSWERING.toString());
                jedis.publish("game_events", "game_state_change");

                RpcResponse.Builder builder = RpcResponse.newBuilder();
                AdminSetActiveQuestionResponse.Builder responseBuilder = AdminSetActiveQuestionResponse.newBuilder();
                responseBuilder.setNewState(newState);
                builder.setAdminSetActiveQuestionResponse(responseBuilder);
                return builder.build();

            } catch (InvalidProtocolBufferException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class SetGameStateHandler implements IRpcHandler {
        @Override
        public RpcResponse handle(String currentUserId, RpcRequest request, Session session) {
            AdminSetGameStateRequest wrapped = request.getAdminSetGameStateRequest();
            jedis.hset("state", "state", wrapped.getNewState().toString());
            jedis.publish("game_events", "game_state_change");

            RpcResponse.Builder builder = RpcResponse.newBuilder();
            AdminSetGameStateResponse.Builder responseBuilder = AdminSetGameStateResponse.newBuilder();
            responseBuilder.setNewState(RedisHelpers.getGameState(null));
            builder.setAdminSetGameStateResponse(responseBuilder);
            return builder.build();
        }
    }
}
