package club.eslcc.bigsciencequiz.server;

import java.util.Arrays;

/**
 * Created by marks on 11/03/2017.
 */
public class EventHelpers {
    public static byte[] addEventFlag(byte[] data) {
        byte[] result = new byte[data.length + 4];
        Arrays.fill(result, 0, 4, (byte) 0xff);
        System.arraycopy(data, 0, result, 4, data.length);
        return result;
    }
}
