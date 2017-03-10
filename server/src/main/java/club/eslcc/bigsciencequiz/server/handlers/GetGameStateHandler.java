package club.eslcc.bigsciencequiz.server.handlers;

import static club.eslcc.bigsciencequiz.proto.Rpc.*;

import club.eslcc.bigsciencequiz.server.IRpcHandler;
import club.eslcc.bigsciencequiz.server.RedisHelpers;

/**
 * Created by marks on 10/03/2017.
 */
public class GetGameStateHandler implements IRpcHandler<GetGameStateRequest> {
    @Override
    public RpcResponse handle(String currentUserId, RpcRequest request) {
        RpcResponse.Builder builder = RpcResponse.newBuilder();
        GetGameStateResponse.Builder responseBuilder = GetGameStateResponse.newBuilder();
        responseBuilder.setState(RedisHelpers.getGameState(currentUserId));
        builder.setGetGameStateResponse(responseBuilder);
        return builder.build();
    }
}
