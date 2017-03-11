package club.eslcc.bigsciencequiz.server;

import club.eslcc.bigsciencequiz.server.handlers.AdminHandlers;
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
import java.util.*;
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
    static Map<Session, String> users = Collections.synchronizedMap(new HashMap<>());
    private static Jedis jedis = Redis.getJedis();
    private static Jedis pubSubJedis = Redis.getNewJedis();
    private static volatile boolean subscribed = false;

    private static Map<RpcRequest.RequestCase, IRpcHandler> handlers = new HashMap<>();

    static {
        handlers.put(RpcRequest.RequestCase.GETGAMESTATEREQUEST, new GetGameStateHandler());
        handlers.put(RpcRequest.RequestCase.IDENTIFYUSERREQUEST, new IdentifyUserHandler());
        handlers.put(RpcRequest.RequestCase.ADMINSETACTIVEQUESTIONREQUEST, new AdminHandlers.ActivateQuestionHandler());
    }

    private static void subscribe() {
        pubSubJedis.subscribe(new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                System.out.println("Got message " + message);
                switch (channel) {
                    case "game_events":
                        switch (message) {
                            case "game_state_change":
                                users.keySet().stream().filter(Session::isOpen).forEach(session -> {
                                    GameEvent.Builder builder = GameEvent.newBuilder();
                                    GameStateChangeEvent.Builder gsceB = GameStateChangeEvent.newBuilder();
                                    gsceB.setNewState(RedisHelpers.getGameState(users.get(session)));
                                    builder.setGameStateChangeEvent(gsceB);
                                    GameEvent event = builder.build();
                                    try {
                                        session.getRemote().sendBytes(event.toByteString().asReadOnlyByteBuffer());
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                });
                                break;
                        }
                        break;
                }
            }
        }, "game_events");
    }

    @OnWebSocketConnect
    public void connect(Session session) {
        Logger logger = Logger.getGlobal();
        logger.log(Level.FINE, "hello from connect");
        session.getUpgradeResponse().setHeader("X-Horsemeat", "Swedish");

        if (session.getUpgradeRequest().getParameterMap().containsKey("admin")) {
            users.put(session, "ADMIN");
            logger.log(Level.INFO, "Admin connected!");
        } else {
            users.put(session, null);
        }

        if (!subscribed) {
            Thread t = new Thread(SocketHandler::subscribe);
            t.start();
            subscribed = true;
        }
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
