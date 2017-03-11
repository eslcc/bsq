package club.eslcc.bigsciencequiz.server;

import club.eslcc.bigsciencequiz.server.callbacks.InitializeStateCallback;

import java.util.Arrays;
import java.util.List;

import static spark.Spark.*;

/**
 * Created by marks on 10/03/2017.
 */
public class Server {
    @SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
    private static List<IStartupCallback> callbacks = Arrays.asList(new InitializeStateCallback());

    private static void runCallbacks() {
        for (IStartupCallback callback : callbacks) {
            callback.onStartup();
        }
    }

    public static void main(String[] args) {
        runCallbacks();
        staticFileLocation("/");
        webSocket("/socket", SocketHandler.class);
        port(8080);
        init();
    }
}
