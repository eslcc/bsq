package club.eslcc.bigsciencequiz.server;

import java.io.UnsupportedEncodingException;

/**
 * Created by marks on 11/03/2017.
 */
public class RpcHelpers {
    public static byte[] stob(String s) {
        try {
            return s.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            // Literally cannot happen
            throw new RuntimeException(e);
        }
    }
}
