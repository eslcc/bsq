package club.eslcc.bigsciencequiz.server.handlers;

import club.eslcc.bigsciencequiz.proto.Rpc;
import club.eslcc.bigsciencequiz.proto.User;
import club.eslcc.bigsciencequiz.proto.bigscreen.BigscreenRpc;
import club.eslcc.bigsciencequiz.server.IRpcHandler;
import club.eslcc.bigsciencequiz.server.Redis;
import org.eclipse.jetty.websocket.api.Session;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by marks on 19/03/2017.
 */
public class BigscreenHandlers {
    public static class GetTeamsHandler implements IRpcHandler {
        @Override
        public Rpc.RpcResponse handle(String currentUserId, Rpc.RpcRequest wrapper, Session session) {
            try (Jedis jedis = Redis.pool.getResource()) {
                BigscreenRpc.BigscreenGetTeamsRequest request = wrapper.getBigscreenGetTeamsRequest();

                List<String> teamNumbers = jedis.lrange("teams", 0, -1);
                List<User.Team> teams = new ArrayList<>(teamNumbers.size());
                for (String number : teamNumbers) {
                    User.Team.Builder tBuilder = User.Team.newBuilder();
                    tBuilder.setNumber(number);
                    tBuilder.addAllMemberNames(jedis.lrange("team_members_" + number, 0, -1));
                    teams.add(tBuilder.build());
                }

                Rpc.RpcResponse.Builder builder = Rpc.RpcResponse.newBuilder();
                BigscreenRpc.BigscreenGetTeamsResponse.Builder responseBuilder = BigscreenRpc.BigscreenGetTeamsResponse.newBuilder();
                responseBuilder.addAllTeams(teams);
                builder.setBigscreenGetTeamsResponse(responseBuilder);
                return builder.build();
            }
        }
    }
}
