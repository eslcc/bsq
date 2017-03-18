package club.eslcc.bigsciencequiz.server;

import club.eslcc.bigsciencequiz.proto.Events;
import club.eslcc.bigsciencequiz.proto.QuestionOuterClass;
import club.eslcc.bigsciencequiz.proto.admin.AdminEvents;
import com.google.protobuf.InvalidProtocolBufferException;
import org.eclipse.jetty.websocket.api.Session;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static club.eslcc.bigsciencequiz.server.RpcHelpers.itos;
import static club.eslcc.bigsciencequiz.server.RpcHelpers.stob;
import static club.eslcc.bigsciencequiz.server.RpcHelpers.stoi;


/**
 * Created by marks on 18/03/2017.
 */
public class RpcPubSub extends JedisPubSub {

    private void sendEvent(Events.GameEvent event) {
        byte[] data = EventHelpers.addEventFlag(event.toByteArray());
        SocketHandler.users.keySet().stream()
                .filter(s -> SocketHandler.users.get(s) != null)
                .filter(Session::isOpen)
                .forEach(session -> {
                            try {
                                session.getRemote().sendBytes(ByteBuffer.wrap(data));
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                );
    }

    private void sendAdminEvent(Events.GameEvent event) {
        byte[] data = EventHelpers.addEventFlag(event.toByteArray());
        SocketHandler.users.keySet().stream()
                .filter(s -> SocketHandler.users.get(s) != null)
                .filter(s -> SocketHandler.users.get(s).equals("ADMIN"))
                .filter(Session::isOpen)
                .forEach(session -> {
                            try {
                                session.getRemote().sendBytes(ByteBuffer.wrap(data));
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                );
    }

    private void handleGameStateChange() {
        SocketHandler.users.keySet().stream().filter(Session::isOpen).forEach(session -> {
            Events.GameEvent.Builder builder = Events.GameEvent.newBuilder();
            Events.GameStateChangeEvent.Builder gsceB = Events.GameStateChangeEvent.newBuilder();
            gsceB.setNewState(RedisHelpers.getGameState(SocketHandler.users.get(session)));
            builder.setGameStateChangeEvent(gsceB);
            Events.GameEvent event = builder.build();
            byte[] data = EventHelpers.addEventFlag(event.toByteArray());
            try {
                session.getRemote().sendBytes(ByteBuffer.wrap(data));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void handleAdminDevices() {
        try (Jedis jedis = Redis.pool.getResource()) {
            Map<String, String> deviceTeams = jedis.hgetAll("devices");
            Set<String> readyDevices = jedis.smembers("ready_devices");

            List<AdminEvents.AdminDevicesChangedEvent.Device> result = deviceTeams.keySet().stream().map(
                    id -> AdminEvents.AdminDevicesChangedEvent.Device.newBuilder()
                            .setDeviceId(id)
                            .setReady(readyDevices.contains(id))
                            .setTeam(stoi(deviceTeams.get(id)))
                            .build()
            ).collect(Collectors.toList());

            Events.GameEvent.Builder wrapped = Events.GameEvent.newBuilder();
            AdminEvents.AdminDevicesChangedEvent.Builder builder = AdminEvents.AdminDevicesChangedEvent.newBuilder();
            builder.addAllDevices(result);
            wrapped.setAdminDevicesChangedEvent(builder);
            Events.GameEvent event = wrapped.build();
            sendAdminEvent(event);
        }
    }

    private void handleNoFreeTeams(String[] args) {
        Events.GameEvent.Builder wrapped = Events.GameEvent.newBuilder();
        Events.ErrorEvent.Builder builder = Events.ErrorEvent.newBuilder();
        builder.setDescription("No free teams for device " + args[0]);
        wrapped.setErrorEvent(builder);
        Events.GameEvent event = wrapped.build();
        sendAdminEvent(event);
    }

    private void handleQuestionsChange() {
        try (Jedis jedis = Redis.pool.getResource()) {
            Map<byte[], byte[]> questions = jedis.hgetAll(stob("questions"));

            List<QuestionOuterClass.Question> questionProtos = questions.values().stream().map(bytes -> {
                try {
                    return QuestionOuterClass.Question.parseFrom(bytes);
                } catch (InvalidProtocolBufferException e) {
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toList());

            Events.GameEvent.Builder wrapped = Events.GameEvent.newBuilder();
            AdminEvents.AdminQuestionsChangedEvent.Builder builder = AdminEvents.AdminQuestionsChangedEvent.newBuilder();
            builder.addAllNewQuestions(questionProtos);
            wrapped.setAdminQuestionsChangedEvent(builder);
            Events.GameEvent event = wrapped.build();
            sendAdminEvent(event);
        }
    }

    @Override
    public void onMessage(String channel, String message) {
        switch (channel) {
            case "game_events":
                switch (message) {
                    case "game_state_change":
                        handleGameStateChange();
                        break;
                }
                break;
            case "admin_events":
                switch (message) {
                    case "identified_device_change":
                    case "ready_device_change":
                        handleAdminDevices();
                        break;
                    case "questions_change":
                        handleQuestionsChange();
                        break;
                    default:
                        if (message.startsWith("no_free_teams")) {
                            String[] args = message.substring("no_free_teams:".length()).split(":");
                            handleNoFreeTeams(args);
                        } else {
                            System.out.println("WHAT: " + message);
                        }
                }
        }
    }
}
