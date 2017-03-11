package club.eslcc.bigsciencequiz.microtests;

import org.java_websocket.WebSocket;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by marks on 11/03/2017.
 */
public class TestRunner {
    private static List<Tester> testers = new ArrayList<>();

    public static void main(String[] args) throws URISyntaxException {
        int number = 150;
        for (int i = 0; i < number; i++) {
            testers.add(new Tester(i));
        }

        while (testers.stream().allMatch(t -> t.getReadyState() == WebSocket.READYSTATE.OPEN)) ;

        System.out.println("[MASTER] All testers ready.");


        while (true) {
            try {
                for (Tester tester : testers) {
                    tester.nextState();
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                break;
            }
        }

    }
}
