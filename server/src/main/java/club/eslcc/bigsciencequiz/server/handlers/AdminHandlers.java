package club.eslcc.bigsciencequiz.server.handlers;

import static club.eslcc.bigsciencequiz.proto.Gamestate.*;
import static club.eslcc.bigsciencequiz.proto.QuestionOuterClass.*;
import static club.eslcc.bigsciencequiz.server.RpcHelpers.stob;

import club.eslcc.bigsciencequiz.proto.Rpc;
import club.eslcc.bigsciencequiz.proto.admin.AdminRpc;
import club.eslcc.bigsciencequiz.server.IRpcHandler;
import club.eslcc.bigsciencequiz.server.Redis;
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
        public Rpc.RpcResponse handle(String currentUserId, Rpc.RpcRequest request, Session session) {
            Rpc.RpcResponse.Builder builder = Rpc.RpcResponse.newBuilder();
            if (!currentUserId.equals("ADMIN")) {
                Rpc.UnauthorisedRequestResponse.Builder urrB = Rpc.UnauthorisedRequestResponse.newBuilder();
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
                    AdminRpc.AdminGetQuestionsResponse.newBuilder().addAllQuestions(questionProtos).build()
            );
            return builder.build();
        }
    }

    public static class ActivateQuestionHandler implements IRpcHandler {
        @Override
        public Rpc.RpcResponse handle(String currentUserId, Rpc.RpcRequest wrapped, Session session) {
            try {
                AdminRpc.AdminSetActiveQuestionRequest request = wrapped.getAdminSetActiveQuestionRequest();
                GameState state = GameState.parseFrom(jedis.hget(stob("state"), stob("state")));
                GameState.Builder newBuilder = GameState.newBuilder(state);
                Question newQuestion = Question.parseFrom(jedis.hget(
                        stob("questions"), stob(Integer.valueOf(request.getQuestionId()).toString())
                ));
                newBuilder.setCurrentQuestion(newQuestion);
                GameState newState = newBuilder.build();
                jedis.hset(stob("state"), stob("state"), newState.toByteArray());
                jedis.publish("game_events", "game_state_change");

                Rpc.RpcResponse.Builder builder = Rpc.RpcResponse.newBuilder();
                AdminRpc.AdminSetActiveQuestionResponse.Builder responseBuilder = AdminRpc.AdminSetActiveQuestionResponse.newBuilder();
                responseBuilder.setNewState(newState);
                builder.setAdminSetActiveQuestionResponse(responseBuilder);
                return builder.build();

            } catch (InvalidProtocolBufferException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
