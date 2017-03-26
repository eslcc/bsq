package club.eslcc.bigsciencequiz.server.handlers;

import static club.eslcc.bigsciencequiz.proto.Rpc.*;

import club.eslcc.bigsciencequiz.proto.User;
import club.eslcc.bigsciencequiz.server.IRpcHandler;
import club.eslcc.bigsciencequiz.server.Redis;
import org.eclipse.jetty.websocket.api.Session;
import redis.clients.jedis.Jedis;

import java.util.List;

/**
 * Created by marks on 11/03/2017.
 */
public class TeamReadyHandler implements IRpcHandler {
    @Override
    public RpcResponse handle(String currentUserId, RpcRequest request, Session session) {
        try (Jedis jedis = Redis.pool.getResource()) {
            TeamReadyRequest rq = request.getTeamReadyRequest();
            RpcResponse.Builder builder = RpcResponse.newBuilder();
            TeamReadyResponse.Builder responseBuilder = TeamReadyResponse.newBuilder();

            List<String> names = jedis.hvals("team_names");

            if (names.contains(rq.getTeamName())) {
                responseBuilder.setFailureReason(TeamReadyResponse.FailureReason.TEAM_NAME_TAKEN);
                builder.setTeamReadyResponse(responseBuilder);
                return builder.build();
            }

            String teamNumber = jedis.hget("devices", currentUserId);
            List<String> members = jedis.lrange("team_members_" + teamNumber, 0, -1);

            long result = jedis.hsetnx("team_names", teamNumber, rq.getTeamName());

            if (result == 0L) {
                responseBuilder.setFailureReason(TeamReadyResponse.FailureReason.ALREADY_REGISTERED);
            } else {
                jedis.sadd("ready_devices", currentUserId);
                jedis.publish("admin_events", "ready_device_change");

                User.Team.Builder teamBuilder = User.Team.newBuilder();
                teamBuilder.setNumber(teamNumber);
                teamBuilder.addAllMemberNames(members);
                teamBuilder.setTeamName(rq.getTeamName());
                responseBuilder.setTeam(teamBuilder);
            }
            builder.setTeamReadyResponse(responseBuilder);
            return builder.build();
        }
    }
}
