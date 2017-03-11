package club.eslcc.bigsciencequiz.server;

import club.eslcc.bigsciencequiz.server.handlers.GetGameStateHandler;
import club.eslcc.bigsciencequiz.server.handlers.IdentifyUserHandler;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import static club.eslcc.bigsciencequiz.proto.Rpc.*;
import static club.eslcc.bigsciencequiz.proto.Events.*;

/**
 * Created by marks on 10/03/2017.
 */
@WebSocket
public class SocketHandler {
    static Map<Session, String> users = new HashMap<>();
    private static Jedis jedis = Redis.getJedis();

    private static Map<RpcRequest.RequestCase, IRpcHandler> handlers = new HashMap<>();

    static {
        handlers.put(RpcRequest.RequestCase.GETGAMESTATEREQUEST, new GetGameStateHandler());
        handlers.put(RpcRequest.RequestCase.IDENTIFYUSERREQUEST, new IdentifyUserHandler());
    }

    @OnWebSocketConnect
    public void connect(Session session) {
        Logger logger = Logger.getGlobal();
        logger.log(Level.FINE, "hello from connect");
        session.getUpgradeResponse().setHeader("X-Horsemeat", "Swedish");

        if(session.getUpgradeRequest().getParameterMap().containsKey("admin")) {
            users.put(session, "ADMIN");
            logger.log(Level.INFO, "Admin connected!");
        } else {
            users.put(session, null);
        }
//
//        jedis.subscribe(new JedisPubSub() {
//            @Override
//            public void onMessage(String channel, String message) {
//                System.out.println("Got message " + message);
//            }
//        }, "game_events");
    }

    @OnWebSocketClose
    public void disconnect(Session session, int status, String reason) {
//        users.remove(session);
    }

    @OnWebSocketMessage
    public void onMessage(Session session, byte[] buf, int offset, int length) {
        try {
            RpcRequest request = RpcRequest.parseFrom(buf);
            RpcRequest.RequestCase requestCase = request.getRequestCase();
            RpcResponse response;
            if (handlers.containsKey(requestCase)) {
                response = handlers.get(requestCase).handle(users.get(session), request, session);
            } else {
                RpcResponse.Builder builder = RpcResponse.newBuilder();
                UnknownRequestResponse.Builder responseBuilder = UnknownRequestResponse.newBuilder();
                responseBuilder.setRequest(requestCase.toString());
                builder.setUnknownRequestResponse(responseBuilder.build());
                response = builder.build();
            }
            session.getRemote().sendBytes(response.toByteString().asReadOnlyByteBuffer());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
