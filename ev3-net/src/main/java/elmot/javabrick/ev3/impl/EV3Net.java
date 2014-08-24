package elmot.javabrick.ev3.impl;

import elmot.javabrick.ev3.EV3;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class EV3Net extends EV3 {

    public EV3Net(InetAddress address, int port, String serial) {
        super();
        this.address = address;
        this.port = port;
        this.serial = serial;
    }
    public static final byte[] UNLOCK_RESPONSE = "Accept:EV340\r\n\r\n".getBytes(StandardCharsets.US_ASCII);
    private final InetAddress address;
    private final int port;
    private final String serial;
    private InputStream downStream = null;
    private OutputStream upStream = null;
    private Socket socket = null;

    public void ensureOpen() throws IOException {
        if (socket != null) return;
        socket = new Socket(address, port);
        socket.setTcpNoDelay(true);
        upStream = socket.getOutputStream();
        downStream = socket.getInputStream();
        upStream.write(String.format("GET /target?sn=%s VTMP1.0\r\nProtocol: EV3", serial).getBytes(StandardCharsets.US_ASCII));
        upStream.flush();
        byte[] unlockResponse = new byte[16];
        for (int i = 0; i < unlockResponse.length; i++) {
            int b = downStream.read();
            if (b < 0) throw new IOException("Unlock failed");
            unlockResponse[i] = (byte) b;

        }
        if (!Arrays.equals(UNLOCK_RESPONSE, unlockResponse)) {
            throw new IOException("Unlock failed");
        }
    }

    @Override
    public void close() throws Exception {
        upStream.close();
        downStream.close();
        socket.close();
        socket = null;
        upStream = null;
        downStream = null;
    }

    private int read2Bytes(InputStream inputStream) throws IOException {
        int result;
        result = readByte(inputStream) & 0xFF;
        result |= (readByte(inputStream) << 8) & 0xFF00;
        return result;
    }

    private int readByte(InputStream inputStream) throws IOException {
        int b = inputStream.read();
        if (b < 0) throw new IOException("Unexpected EndOfStream");
        return b;
    }

    public synchronized byte[] dataExchange(byte[] bytes) throws IOException {
        upStream.write(bytes);
        upStream.flush();
        int length = read2Bytes(downStream);
        if (length < 3) {
            throw new IOException(String.format("Response format error(length: %d)", length));
        }
        byte[] response = new byte[length + 2];
        response[0] = (byte) (length & 0xff);
        response[1] = (byte) (length >> 8);
        for (int i = 2; i < response.length; i++) {
            response[i] = (byte) readByte(downStream);
        }
        return response;
    }
}
