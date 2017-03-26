package club.eslcc.bigsciencequiz.server;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import redis.clients.jedis.Jedis;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static club.eslcc.bigsciencequiz.proto.Appstate.*;
import static club.eslcc.bigsciencequiz.proto.Gamestate.*;
import static club.eslcc.bigsciencequiz.proto.QuestionOuterClass.*;
import static club.eslcc.bigsciencequiz.proto.User.*;

/**
 * Created by marks on 10/03/2017.
 */
public class RedisHelpers {
    public static Team getTeam(String teamId) {
        try (Jedis jedis = Redis.pool.getResource()) {
            String name = jedis.hget("team_names", teamId);
            List<String> members = jedis.lrange("team_members_" + teamId, 0, -1);
            int score = jedis.zscore("answers", teamId).intValue();

            Team.Builder teamBuilder = Team.newBuilder();
            teamBuilder.setNumber(teamId);
            teamBuilder.setTeamName(name);
            teamBuilder.addAllMemberNames(members);
            teamBuilder.setScore(score);

            return teamBuilder.build();
        }
    }

    public static GameState getGameState() {
        try (Jedis jedis = Redis.pool.getResource()) {
            Map<String, String> state = jedis.hgetAll("state");
            String stateName = state.get("state");
            String currentQuestion = state.get("currentQuestion");

            GameState.Builder builder = GameState.newBuilder();
            builder.setState(GameState.State.valueOf(stateName == null ? "NOTREADY" : stateName));

            if (currentQuestion != null) {
                Question question;

                try {
                    question = Question.parseFrom(ByteString.copyFrom(currentQuestion, "UTF-8"));
                } catch (InvalidProtocolBufferException e) {
                    // Most definitely can happen.
                    throw new IllegalStateException(e);
                } catch (UnsupportedEncodingException e) {
                    // Can't happen.
                    throw new RuntimeException(e);
                }

                builder.setCurrentQuestion(question);
            }

            return builder.build();
        }
    }

    public static AppState getAppState(String userId) {
        GameState gameState = getGameState();
        String teamId = null;
        int userAnswer = -1;
        int correctAnswer = -1;

        try (Jedis jedis = Redis.pool.getResource()) {
            if (userId != null && (!userId.equals("ADMIN")) && (!userId.equals("BIGSCREEN"))) {
                teamId = jedis.hget("devices", userId);
                String answer = jedis.hget("answers", teamId);
                userAnswer = Integer.parseInt(answer);
            }
        }

        Optional<Question.Answer> streamAnswer = gameState.getCurrentQuestion().getAnswersList().stream().filter(Question.Answer::getCorrect).findFirst();
        if (streamAnswer.isPresent())
            correctAnswer = streamAnswer.get().getId();

        AppState.Builder builder = AppState.newBuilder();
        builder.setGameState(getGameState());

        if (teamId != null) {
            builder.setTeam(getTeam(teamId));
        }

        if (userAnswer != -1) {
            builder.setUserAnswer(userAnswer);
        }

        if (correctAnswer != -1) {
            builder.setCorrectAnswer(correctAnswer);
        }

        return builder.build();
    }
}
