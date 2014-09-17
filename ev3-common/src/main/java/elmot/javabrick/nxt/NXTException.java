package elmot.javabrick.nxt;

import java.io.IOException;

/**
 * @author elmot
 *         Date: 08.09.14
 */
public class NXTException extends IOException {

    private final int statusByte;

    public NXTException(String message, int statusByte) {
        super(message);
        this.statusByte = statusByte & 0xFF;
    }

    public NXTException(String message) {
        super(message);
        this.statusByte = -1;

    }

    public NXTException(Exception e) {
        super(e);
        this.statusByte = -1;
    }

    public int getStatusByte() {
        return statusByte;
    }
}
