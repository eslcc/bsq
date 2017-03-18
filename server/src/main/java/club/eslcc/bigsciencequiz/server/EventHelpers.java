package club.eslcc.bigsciencequiz.server;

import java.util.Arrays;

/**
 * Created by marks on 11/03/2017.
 */
public class EventHelpers {
    /**
     * Adds the magic number (four times 0xFF at the start) indicating that a proto message is an event
     * @param data The data to be wrapped
     * @return The data, with the event flag
     */
    public static byte[] addEventFlag(byte[] data) {
        byte[] result = new byte[data.length + 4];
        Arrays.fill(result, 0, 4, (byte) 0xff);
        System.arraycopy(data, 0, result, 4, data.length);
        return result;
    }
}
