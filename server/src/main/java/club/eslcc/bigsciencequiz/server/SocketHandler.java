package club.eslcc.bigsciencequiz.server;

import club.eslcc.bigsciencequiz.proto.Events;
import club.eslcc.bigsciencequiz.server.handlers.*;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static club.eslcc.bigsciencequiz.proto.Rpc.*;
import static club.eslcc.bigsciencequiz.proto.Rpc.RpcRequest.RequestCase.*;

/**
 * Created by marks on 10/03/2017.
 */
@WebSocket
public class SocketHandler {
    public static Map<Session, String> users = Collections.synchronizedMap(new HashMap<>());
    private static Jedis pubSubJedis = Redis.getNewJedis();
    private static volatile boolean subscribed = false;

    public static final Object logLock = new Object();

    private static Map<RpcRequest.RequestCase, IRpcHandler> handlers = new HashMap<>();

    static {
        handlers.put(GETGAMESTATEREQUEST, new GetGameStateHandler());
        handlers.put(IDENTIFYUSERREQUEST, new IdentifyUserHandler());
        handlers.put(ADMINSETACTIVEQUESTIONREQUEST, new AdminHandlers.ActivateQuestionHandler());
        handlers.put(ADMINGETQUESTIONSREQUEST, new AdminHandlers.GetQuestionsHandler());
        handlers.put(TEAMREADYREQUEST, new TeamReadyHandler());
        handlers.put(ANSWERQUESTIONREQUEST, new AnswerQuestionHandler());
        handlers.put(ADMINSETGAMESTATEREQUEST, new AdminHandlers.SetGameStateHandler());
        handlers.put(BIGSCREENGETTEAMSREQUEST, new BigscreenHandlers.GetTeamsHandler());
        handlers.put(ADMINSHUTDOWNDEVICEREQUEST, new AdminHandlers.ShutdownHandler());
    }

    private static void subscribe() {
        JedisPubSub handler = new RpcPubSub();
        pubSubJedis.subscribe(handler, "game_events", "admin_events", "bigscreen_events", "system_messages", "device_shutdown");
    }

    @OnWebSocketConnect
    public void connect(Session session) {
        Logger logger = Logger.getGlobal();
        logger.log(Level.FINE, "hello from connect");
        session.getUpgradeResponse().setHeader("X-Horsemeat", "Swedish");

        if (session.getUpgradeRequest().getParameterMap().containsKey("admin")) {
            users.put(session, "ADMIN");
            logger.log(Level.INFO, "Admin connected!");
        } else if (session.getUpgradeRequest().getParameterMap().containsKey("bigscreen")) {
            users.put(session, "BIGSCREEN");
            logger.log(Level.INFO, "Bigscreen connected!");
        } else {
            users.put(session, null);
        }

        if (!subscribed) {
            Thread t = new Thread(SocketHandler::subscribe);
            t.start();
            subscribed = true;
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutdown hook running");
            Events.GameEvent.Builder wrapped = Events.GameEvent.newBuilder();
            Events.ReconnectEvent.Builder builder = Events.ReconnectEvent.newBuilder();
            wrapped.setReconnectEvent(builder);
            Events.GameEvent event = wrapped.build();
            byte[] data = EventHelpers.addEventFlag(event.toByteArray());
            try {
                session.getRemote().sendBytes(ByteBuffer.wrap(data));
            } catch (IOException e) {
                System.out.println("Error during shutdown!");
                e.printStackTrace();
            }
        }));
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

            System.out.println("Got request: " + requestCase);
            System.out.println("Content: " + request);

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
            synchronized (logLock) {
                System.out.println("Sent response: " + response.getResponseCase());
                System.out.println("Content: " + response);
                System.out.println("Binary: " + Arrays.toString(response.toByteArray()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
