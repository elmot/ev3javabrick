package elmot.javabrick.ev3.impl;

import elmot.javabrick.ev3.EV3;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;
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
    private SocketChannel socketChannel = null;

    public void ensureOpen() throws IOException {
        if (socketChannel != null) return;
        socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress(address, port));
        socketChannel.setOption(StandardSocketOptions.SO_KEEPALIVE,true);
        socketChannel.setOption(StandardSocketOptions.TCP_NODELAY, true);
        socketChannel.configureBlocking(true);
        String initMsg = String.format("GET /target?sn=%s VTMP1.0\r\nProtocol: EV3", serial);
        ByteBuffer byteBuffer = ByteBuffer.allocate(initMsg.length());
        byteBuffer.put(initMsg.getBytes(StandardCharsets.US_ASCII));
        socketChannel.finishConnect();
        byteBuffer.rewind();
        socketChannel.write(byteBuffer);
        ByteBuffer unlockResponse = ByteBuffer.allocate(16).order(ByteOrder.LITTLE_ENDIAN);
        while (unlockResponse.remaining() > 0) {
            socketChannel.read(unlockResponse);
        }
        if (!Arrays.equals(UNLOCK_RESPONSE, unlockResponse.array())) {
            throw new IOException("Unlock failed");
        }
    }

    @Override
    public void close() throws Exception {
        socketChannel.close();
        socketChannel = null;
    }

    @Override
    public synchronized ByteBuffer dataExchange(ByteBuffer bytes, int expectedSeqNo) throws IOException {
        socketChannel.write(bytes);
        ByteBuffer response = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
        response.limit(2);
        while (response.remaining() > 0)
            socketChannel.read(response);
        int length = response.getShort(0);
        if (length < 3) {
            throw new IOException(String.format("Response format error(length: %d)", length));
        }
        if (length + 2 > response.capacity()) {
            response = ByteBuffer.allocate(length + 2).order(ByteOrder.LITTLE_ENDIAN);
            response.putShort(0, (short) length);
            response.position(2);
        }
        response.limit(length+2);
        while (response.remaining() >0)
        {
            socketChannel.read(response);
        }
        return response;
    }
}
