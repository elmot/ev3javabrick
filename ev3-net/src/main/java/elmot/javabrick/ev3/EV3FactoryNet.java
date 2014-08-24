package elmot.javabrick.ev3;

import elmot.javabrick.ev3.impl.EV3Net;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author elmot
 */
public class EV3FactoryNet implements AutoCloseable {

    public static final int PORT = 3015;
    public static final int DISCOVERY_TIMEOUT = 10000;

    private final InetAddress listenAddress;
    private DatagramSocket socket;
    private Thread listener;
    private ConcurrentHashMap<String, BrickDiscovery> discoveries = new ConcurrentHashMap<String, BrickDiscovery>();

    public EV3FactoryNet(InetAddress listenAddress) throws SocketException {
        this.listenAddress = listenAddress;
    }

    public EV3FactoryNet() throws SocketException {
        this(null);
    }

    public synchronized void open() throws SocketException {
        socket = new DatagramSocket(PORT, listenAddress);
        listener = new ListenerThread();
        listener.setDaemon(true);
        listener.start();
    }

    public void waitForDiscovery() {
        try {
            Thread.sleep(6000);
        } catch (InterruptedException ignored) {
        }
    }

    private static class BrickDiscovery {
        public volatile long lastTimestamp;
        public final EV3 brick;

        private BrickDiscovery(EV3 brick, long timestamp) {
            this.brick = brick;
            this.lastTimestamp = timestamp;
        }
    }

    private synchronized void registerBlock(InetAddress address, int port, String serial) {
        BrickDiscovery brickDiscovery = discoveries.get(serial);
        if (brickDiscovery == null) {
            EV3 brick = new EV3Net(address, port, serial);
            brickDiscovery = new BrickDiscovery(brick, System.currentTimeMillis());
            discoveries.put(serial, brickDiscovery);
        } else {
            brickDiscovery.lastTimestamp = System.currentTimeMillis();
        }
        dropExpired();
    }

    private synchronized void dropExpired() {
        long timeWatermark = System.currentTimeMillis() - DISCOVERY_TIMEOUT;
        for (Iterator<BrickDiscovery> iterator = discoveries.values().iterator();
             iterator.hasNext(); ) {
            BrickDiscovery brickDiscovery = iterator.next();
            if (brickDiscovery.lastTimestamp < timeWatermark) iterator.remove();
        }
    }

    public synchronized List<EV3> listDiscovered() {
        dropExpired();
        ArrayList<EV3> ev3s = new ArrayList<EV3>(discoveries.size());
        for (BrickDiscovery brickDiscovery : discoveries.values()) {
            ev3s.add(brickDiscovery.brick);
        }
        return ev3s;
    }

    @Override
    public synchronized void close() {
        if (socket != null) {
            listener.interrupt();
            socket.close();
        }
    }

    private class ListenerThread extends Thread {
        public ListenerThread() {
            super("EV3Listener");
        }

        @Override
        public void run() {
            byte[] buf = new byte[67];
            DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length);
            try {
                while (!isInterrupted()) {
                    try {
                        socket.receive(datagramPacket);
                        InetAddress brickAddress = datagramPacket.getAddress();
                        String discoveryText = new String(datagramPacket.getData(), StandardCharsets.US_ASCII);
                        MessageFormat messageFormat = new MessageFormat("Serial-Number: {0}\r\nPort: {1,number}\r\nName: EV3\r\nProtocol: EV3\r\n");
                        Object[] parsed = (Object[]) messageFormat.parseObject(discoveryText);
                        registerBlock(brickAddress, ((Number) parsed[1]).intValue(), (String) parsed[0]);
                    } catch (ParseException ignored) {
                    }
                }
            } catch (IOException e) {
                if (!isInterrupted()) {
                    e.printStackTrace();
                }
            }
        }
    }
}
