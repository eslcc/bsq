package club.eslcc.bigsciencequiz.server.handlers;

import club.eslcc.bigsciencequiz.proto.Rpc;
import club.eslcc.bigsciencequiz.server.IRpcHandler;
import club.eslcc.bigsciencequiz.server.RedisHelpers;
import org.eclipse.jetty.websocket.api.Session;

/**
 * Created by Vidminas on 26/03/2017.
 */
public class GetAppStateHandler implements IRpcHandler {
    @Override
    public Rpc.RpcResponse handle(String currentUserId, Rpc.RpcRequest request, Session session) {
        Rpc.RpcResponse.Builder builder = Rpc.RpcResponse.newBuilder();
        Rpc.GetAppStateResponse.Builder responseBuilder = Rpc.GetAppStateResponse.newBuilder();
        responseBuilder.setAppState(RedisHelpers.getAppState(currentUserId));
        builder.setGetAppStateResponse(responseBuilder);

        return builder.build();
    }
}
