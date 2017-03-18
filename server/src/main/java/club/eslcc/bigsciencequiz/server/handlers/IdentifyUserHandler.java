package club.eslcc.bigsciencequiz.server.handlers;

import static club.eslcc.bigsciencequiz.proto.Rpc.*;

import club.eslcc.bigsciencequiz.proto.User;
import club.eslcc.bigsciencequiz.server.IRpcHandler;
import club.eslcc.bigsciencequiz.server.Redis;
import club.eslcc.bigsciencequiz.server.RedisHelpers;
import club.eslcc.bigsciencequiz.server.SocketHandler;
import org.eclipse.jetty.websocket.api.Session;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Optional;

/**
 * Created by marks on 11/03/2017.
 */
public class IdentifyUserHandler implements IRpcHandler {
    private static Jedis jedis = Redis.getJedis();

    @Override
    public RpcResponse handle(String currentUserId, RpcRequest request, Session session) {
        RpcResponse.Builder builder = RpcResponse.newBuilder();
        IdentifyUserResponse.Builder responseBuilder = IdentifyUserResponse.newBuilder();

        IdentifyUserRequest idR = request.getIdentifyUserRequest();

        List<String> teams = jedis.lrange("teams", 0, -1);
        List<String> teamNumbers = jedis.hvals("devices");
        Optional<String> firstUnassigned = teams.stream().filter((team) -> !teamNumbers.contains(team)).findFirst();

        if (firstUnassigned.isPresent()) {
            List<String> members = jedis.lrange("team_members_" + firstUnassigned.get(), 0, -1);

            jedis.hset("devices", idR.getDeviceId(), firstUnassigned.get());
            SocketHandler.users.put(session, idR.getDeviceId());

            User.Team.Builder teamBuilder = User.Team.newBuilder();
            teamBuilder.setNumber(firstUnassigned.get());
            teamBuilder.addAllMemberNames(members);

            responseBuilder.setTeam(teamBuilder);
            responseBuilder.setState(RedisHelpers.getGameState(idR.getDeviceId()));
        } else {
            responseBuilder.setFailureReason(IdentifyUserResponse.FailureReason.NO_FREE_TEAMS);
        }
        jedis.publish("admin_events", "identified_device_change");
        builder.setIdentifyUserResponse(responseBuilder);
        return builder.build();
    }
}
