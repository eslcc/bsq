package club.eslcc.bigsciencequiz.server.handlers;

import static club.eslcc.bigsciencequiz.proto.Rpc.*;
import static club.eslcc.bigsciencequiz.server.RpcHelpers.itos;

import club.eslcc.bigsciencequiz.proto.Appstate;
import club.eslcc.bigsciencequiz.proto.Gamestate;
import club.eslcc.bigsciencequiz.proto.QuestionOuterClass;
import club.eslcc.bigsciencequiz.server.IRpcHandler;
import club.eslcc.bigsciencequiz.server.Redis;
import club.eslcc.bigsciencequiz.server.RedisHelpers;
import org.eclipse.jetty.websocket.api.Session;
import redis.clients.jedis.Jedis;

/**
 * Created by marks on 13/03/2017.
 */
public class AnswerQuestionHandler implements IRpcHandler {

    @Override
    public RpcResponse handle(String currentUserId, RpcRequest wrapped, Session session) {
        try (Jedis jedis = Redis.pool.getResource()) {
            AnswerQuestionRequest request = wrapped.getAnswerQuestionRequest();
            RpcResponse.Builder builder = RpcResponse.newBuilder();
            AnswerQuestionResponse.Builder responseBuilder = AnswerQuestionResponse.newBuilder();

            if (currentUserId == null) {
                responseBuilder.setFailureReason(AnswerQuestionResponse.FailureReason.NOT_IDENTIFIED);
            } else {
                Appstate.AppState appState = RedisHelpers.getAppState(currentUserId);
                Gamestate.GameState gameState = appState.getGameState();

                if (gameState.getState() != Gamestate.GameState.State.QUESTION_ANSWERING
                 && gameState.getState() != Gamestate.GameState.State.QUESTION_LIVEANSWERS) {
                    responseBuilder.setFailureReason(AnswerQuestionResponse.FailureReason.INVALID_STATE);
                } else {
                    if (appState.getUserAnswer() != 0) {
                        responseBuilder.setFailureReason(AnswerQuestionResponse.FailureReason.ALREADY_ANSWERED);
                    } else {
                        int answer = request.getAnswerId();
                        QuestionOuterClass.Question currentQuestion = gameState.getCurrentQuestion();
                        if (answer < currentQuestion.getAnswers(0).getId() || answer > currentQuestion.getAnswers(currentQuestion.getAnswersCount() - 1).getId()) {
                            responseBuilder.setFailureReason(AnswerQuestionResponse.FailureReason.OUT_OF_RANGE);
                        } else {
                            String teamId = jedis.hget("devices", currentUserId);
                            jedis.hset("answers", teamId, itos(answer));
                            jedis.zincrby("answer_counts", 1, itos(answer));
                            jedis.publish("game_events", "live_answers");
                            responseBuilder.setFailureReason(AnswerQuestionResponse.FailureReason.NONE);
                        }
                    }
                }
            }

            builder.setAnswerQuestionResponse(responseBuilder);
            return builder.build();
        }
    }
}
