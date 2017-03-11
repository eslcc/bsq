package club.eslcc.bigsciencequiz.server;

import club.eslcc.bigsciencequiz.proto.Rpc;

/**
 * Created by marks on 10/03/2017.
 */
public interface IRpcHandler {
    Rpc.RpcResponse handle(String currentUserId, Rpc.RpcRequest request);
}
