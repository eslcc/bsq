package club.eslcc.bigsciencequiz.server.handlers;

import static club.eslcc.bigsciencequiz.proto.Rpc.*;

import club.eslcc.bigsciencequiz.server.IRpcHandler;
import club.eslcc.bigsciencequiz.server.RedisHelpers;
import club.eslcc.bigsciencequiz.server.SocketHandler;
import org.eclipse.jetty.websocket.api.Session;

/**
 * Created by marks on 11/03/2017.
 */
public class IdentifyUserHandler implements IRpcHandler {
    @Override
    public RpcResponse handle(String currentUserId, RpcRequest request, Session session) {
        RpcResponse.Builder builder = RpcResponse.newBuilder();
        IdentifyUserResponse.Builder responseBuilder = IdentifyUserResponse.newBuilder();

        IdentifyUserRequest idR = request.getIdentifyUserRequest();

//        SocketHandler.users.put(session, idR.getDeviceId());

        responseBuilder.setState(RedisHelpers.getGameState(idR.getDeviceId()));

        return builder.build();
    }
}
