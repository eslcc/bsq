package club.eslcc.bigsciencequiz.server;

import club.eslcc.bigsciencequiz.proto.Rpc;
import org.eclipse.jetty.websocket.api.Session;

/**
 * Created by marks on 10/03/2017.
 */
public interface IRpcHandler {
    Rpc.RpcResponse handle(String currentUserId, Rpc.RpcRequest request, Session session);
}
