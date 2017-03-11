package club.eslcc.bigsciencequiz.microtests;

import club.eslcc.bigsciencequiz.proto.Rpc;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;

import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Random;

/**
 * Created by marks on 11/03/2017.
 */
public class Tester extends WebSocketClient {
    public Tester(int index) throws URISyntaxException {
        super(new URI("ws://localhost:8080/socket"), new Draft_17());
        this.index = index;
        this.connect();
    }

    private int index;

    public TesterState getState() {
        return state;
    }

    private String randomHexString() {
        return randomHexString(16);
    }

    private String randomHexString(int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append(String.format("%x",(int)(Math.random()*100)));
        }
        return builder.toString();
    }

    private TesterState transition(TesterState to) {
        Rpc.RpcRequest.Builder wrapper = Rpc.RpcRequest.newBuilder();
        switch (to) {
            case IDENTIFIED:
                Rpc.IdentifyUserRequest.Builder builder = Rpc.IdentifyUserRequest.newBuilder();
                builder.setDeviceId(randomHexString());
                wrapper.setIdentifyUserRequest(builder);
                Rpc.RpcRequest data = wrapper.build();
                System.out.println(String.format("[%s]: sending %s", this.index, data));;
                this.send(data.toByteArray());
                break;
        }
        state = to;
        return to;
    }

    public TesterState nextState() {
        switch (state) {
            case INITIAL:
                return transition(TesterState.IDENTIFIED);
            default:
                throw new IllegalArgumentException("wat " + state);
        }
    }

    private TesterState state = TesterState.INITIAL;

    @Override
    public void onOpen(ServerHandshake serverHandshake) {

    }

    @Override
    public void onMessage(String s) {

    }

    @Override
    public void onClose(int i, String s, boolean b) {

    }

    @Override
    public void onError(Exception e) {

    }
}
