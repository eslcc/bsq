package club.eslcc.bigsciencequiz.server;

import club.eslcc.bigsciencequiz.server.callbacks.InitializeStateCallback;
import club.eslcc.bigsciencequiz.server.callbacks.LoadQuestionsCallback;
import club.eslcc.bigsciencequiz.server.callbacks.LoadTeamsCallback;

import java.util.Arrays;
import java.util.List;

import static spark.Spark.*;

/**
 * Created by marks on 10/03/2017.
 */
public class Server {
    public static final boolean PROD = System.getenv("PROD") != null;

    @SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
    private static List<IStartupCallback> callbacks = Arrays.asList(
            new InitializeStateCallback(),
            new LoadTeamsCallback(),
            new LoadQuestionsCallback()
    );

    private static void runCallbacks() {
        for (IStartupCallback callback : callbacks) {
            callback.onStartup();
        }
    }

    private static void enableCORS(final String origin, final String methods, final String headers) {

        options("/*", (request, response) -> {

            String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }

            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }

            return "OK";
        });

        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", origin);
            response.header("Access-Control-Request-Method", methods);
            response.header("Access-Control-Allow-Headers", headers);
            // Note: this may or may not be necessary in your particular application
            response.type("application/json");
        });
    }

    public static void main(String[] args) {
        String serverSentryDsn = System.getenv("SENTRY_DSN");
        if (serverSentryDsn != null) {

        }

        runCallbacks();
        staticFiles.location("static/");
        staticFiles.header("Access-Control-Allow-Origin", "*");
        port(8080);
        webSocket("/socket", SocketHandler.class);
        enableCORS("*", "*", "*");
        redirect.get("/admin", "/admin/index.html");
        init();
    }
}
