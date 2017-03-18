package club.eslcc.bigsciencequiz.server;

import club.eslcc.bigsciencequiz.proto.Events;
import club.eslcc.bigsciencequiz.proto.admin.AdminEvents;
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
import static club.eslcc.bigsciencequiz.server.RpcHelpers.stoi;


/**
 * Created by marks on 18/03/2017.
 */
public class RpcPubSub extends JedisPubSub {
    private Jedis jedis = Redis.getJedis();

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
        Map<String, String> deviceTeams = jedis.hgetAll("devices");
        Set<String> readyDevices = jedis.smembers("ready_devices");

        List<AdminEvents.AdminDevicesChangedEvent.Device> result = deviceTeams.keySet().stream().map(
                id -> AdminEvents.AdminDevicesChangedEvent.Device.newBuilder()
                        .setDeviceId(id)
                        .setReady(readyDevices.contains(id))
                        .setTeam(stoi(deviceTeams.get(id)))
                        .build()
        ).collect(Collectors.toList());

        AdminEvents.AdminDevicesChangedEvent.Builder builder = AdminEvents.AdminDevicesChangedEvent.newBuilder();
        builder.addAllDevices(result);
        byte[] data = EventHelpers.addEventFlag(builder.build().toByteArray());

        SocketHandler.users.keySet().stream()
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
                }
        }
    }
}
