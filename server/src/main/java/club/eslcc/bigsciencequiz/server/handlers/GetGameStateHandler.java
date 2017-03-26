package club.eslcc.bigsciencequiz.server.handlers;

import static club.eslcc.bigsciencequiz.proto.Rpc.*;

import club.eslcc.bigsciencequiz.server.IRpcHandler;
import club.eslcc.bigsciencequiz.server.RedisHelpers;
import org.eclipse.jetty.websocket.api.Session;

/**
 * Created by marks on 10/03/2017.
 */
public class GetGameStateHandler implements IRpcHandler {
    @Override
    public RpcResponse handle(String currentUserId, RpcRequest request, Session session) {
        RpcResponse.Builder builder = RpcResponse.newBuilder();
        GetGameStateResponse.Builder responseBuilder = GetGameStateResponse.newBuilder();

        responseBuilder.setGameState(RedisHelpers.getGameState());
        builder.setGetGameStateResponse(responseBuilder);

        return builder.build();
    }
}
