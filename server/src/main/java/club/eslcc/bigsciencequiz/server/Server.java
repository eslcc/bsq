package club.eslcc.bigsciencequiz.server;

import static spark.Spark.*;

/**
 * Created by marks on 10/03/2017.
 */
public class Server {
    public static void main(String[] args) {
        staticFileLocation("/");
        webSocket("/socket", SocketHandler.class);
        port(8080);
        init();
    }
}
