package elmot.ros.web;

import com.google.gson.Gson;
import elmot.javabrick.ev3.EV3;
import elmot.javabrick.ev3.EV3FactoryUsb;
import elmot.ros.ev3.Ev3Node;
import elmot.ros.ev3.Settings;
import fi.iki.elonen.NanoWebSocketServer;
import fi.iki.elonen.WebSocket;
import fi.iki.elonen.WebSocketFrame;
import geometry_msgs.Twist;
import nav_msgs.Odometry;
import org.ros.RosCore;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.*;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;
import std_msgs.Bool;
import std_msgs.Float32;
import std_msgs.Int16;

import java.io.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author elmot
 *         Date: 31.08.14
 */
public class Server extends NanoWebSocketServer implements NodeMain {
    public static final String INDEX_HTML = "/index.html";
    public static final File BASE_DIR = new File("D:\\projects\\elmot-javabrick\\ros-ev3-node\\src\\main\\resources\\elmot\\ros\\web\\");

    private volatile WebSocket latestSocket;
    private Publisher<Twist> cmdVelPubliher;
    private Publisher<Bool> gripPubliher;
    private Publisher<Int16> beepPubliher;

    private static String readResource(String uri) {
        File resource = new File(BASE_DIR, uri);
        StringBuilder result = new StringBuilder();

        try (InputStream is = resource.exists() ? new BufferedInputStream(new FileInputStream(resource))
                : Server.class.getResourceAsStream(uri);
        ) {
            Reader reader = new InputStreamReader(is);
            for (int c; (c = reader.read()) >= 0; ) {
                result.append((char) c);
            }
        } catch (IOException ignored) {
        }
        return result.toString();
    }

    public Server(int port) {
        super(port);
    }

    public Server(String hostname, int port) {
        super(hostname, port);
    }

    private boolean button1 = false;
    private boolean button2 = false;
    private double x = 0;
    private double y = 0;

    @Override
    public WebSocket openWebSocket(IHTTPSession handshake) {
        WebSocket webSocket = new WebSocket(handshake) {
            @Override
            protected void onPong(WebSocketFrame pongFrame) {
                commandVelocity();
            }

            @Override
            protected void onMessage(WebSocketFrame messageFrame) {
                String textPayload = messageFrame.getTextPayload();
                try {
                    if (textPayload != null && textPayload.length() > 0) {
                        Map<?, ?> map = new Gson().fromJson(textPayload, Map.class);
                        Map<String, Double> joystick = (Map<String, Double>) map.get("joystick1");
                        if (joystick != null) {
                            x = -joystick.get("x");
                            y = -joystick.get("y");
                        }
                        Boolean b1 = (Boolean) map.get("button1");
                        if (b1 != null) {
                            if (button1 != b1.booleanValue()) {
                                button1 = b1.booleanValue();
                                Bool msg = gripPubliher.newMessage();
                                msg.setData(button1);
                                gripPubliher.publish(msg);
                            }
                        }
                        Boolean b2 = (Boolean) map.get("button2");
                        if (b2 != null) {
                            if (!button2 && b2.booleanValue()) {
                                Int16 msg = beepPubliher.newMessage();
                                msg.setData((short) 400);
                                beepPubliher.publish(msg);
                            }
                            button2 = b2.booleanValue();
                        }
                    }
                    commandVelocity();
                } catch (RuntimeException e) {
                    System.err.println("JSON: " + textPayload);
                    e.printStackTrace();

                }
            }

            @Override
            protected void onClose(WebSocketFrame.CloseCode code, String reason, boolean initiatedByRemote) {
                setSocket(this, null);
                System.out.println("Close");
            }

            @Override
            protected void onException(IOException e) {
                setSocket(this, null);
            }
        };
        setSocket(null, webSocket);
        return webSocket;
    }

    private void commandVelocity() {
        Twist twist = cmdVelPubliher.newMessage();
        twist.getLinear().setX(x);
        twist.getAngular().setZ(y);
        cmdVelPubliher.publish(twist);
    }

    private synchronized void setSocket(WebSocket oldWebSocket, WebSocket newWebSocket) {
        if (latestSocket == oldWebSocket) latestSocket = newWebSocket;
    }

    public static void main(String[] args) throws IOException {
        Server server = new Server(88);
        RosCore rosCore = RosCore.newPublic(11311);
        rosCore.start();
        String host = System.getenv("ROS_HOSTNAME");
        if (host == null) host = System.getenv("ROS_IP");
        if (host == null) {
            NetworkInterface wlan0 = NetworkInterface.getByName("wlan0");
            if (wlan0 != null) {
                Enumeration<InetAddress> inetAddresses = wlan0.getInetAddresses();
                if (inetAddresses.hasMoreElements()) host = inetAddresses.nextElement().getHostAddress();
            }
        }
        if (host == null) host = InetAddress.getLocalHost().getHostName();
        NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(host);
        System.out.println("Starting at " + host);
        NodeMainExecutor nodeMainExecutor = DefaultNodeMainExecutor.newDefault();
        nodeMainExecutor.execute(server, nodeConfiguration);
        List<EV3> ev3s = EV3FactoryUsb.listDiscovered();
        if (ev3s == null || ev3s.isEmpty()) throw new RuntimeException("No bricks found");
        Ev3Node ev3Node = new Ev3Node(ev3s.get(0), Settings.NODE_NAME.join("usb"), Settings.INSTANCE_NAME, Settings.SAMPLING_LOOP_MS,
                Settings.WHEEL_RADIUS_CM, Settings.WHEEL_DISTANCE_CM);
        nodeMainExecutor.execute(ev3Node, nodeConfiguration);
        //noinspection ResultOfMethodCallIgnored
        System.in.read();
    }

    @SuppressWarnings("deprecation")
    @Override
    public Response serve(String uri, Method method, Map<String, String> headers, Map<String, String> parms, Map<String, String> files) {
        if (INDEX_HTML.equals(uri)) {
            return new Response(readResource("index.html"));
        }
        Response response = new Response(Response.Status.REDIRECT, MIME_PLAINTEXT, "goto /index.html");
        response.addHeader("location", INDEX_HTML);
        return response;
    }

    @Override
    public void onStart(ConnectedNode connectedNode) {
        try {
            start();
            cmdVelPubliher = connectedNode.newPublisher(Settings.INSTANCE_NAME.join("cmd_vel"), Twist._TYPE);
            beepPubliher = connectedNode.newPublisher(Settings.INSTANCE_NAME.join("tone"), Int16._TYPE);
            gripPubliher = connectedNode.newPublisher(Settings.INSTANCE_NAME.join("grip"), Bool._TYPE);
            odoSubscriber(connectedNode, "odom");
            voltageSubscriber(connectedNode, "voltage");
            consumptionSubscriber(connectedNode, "consumption");
            irSubscriber(connectedNode, "ir_distance");

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void odoSubscriber(ConnectedNode connectedNode, String topicNameSuffix) {
        GraphName join = Settings.INSTANCE_NAME.join(topicNameSuffix);
        Subscriber<Odometry> speed = connectedNode.newSubscriber(join, Odometry._TYPE);
        speed.addMessageListener(
                new MessageListener<Odometry>() {
                    @Override
                    public void onNewMessage(Odometry message) {
                        Twist twist = message.getTwist().getTwist();
                        sendData("spd", format("%6.3f", twist.getLinear().getX()),
                                "dir", format("%4.0f", (180 * twist.getAngular().getZ() / Math.PI) % 360));
                    }
                }
        );
    }

    private void voltageSubscriber(ConnectedNode connectedNode, String topicNameSuffix) {
        GraphName name = Settings.INSTANCE_NAME.join(topicNameSuffix);
        Subscriber<Float32> subscriber = connectedNode.newSubscriber(name, Float32._TYPE);
        subscriber.addMessageListener(
                new MessageListener<Float32>() {
                    @Override
                    public void onNewMessage(Float32 message) {
                        sendData("vbatt", format("%6.2f", message.getData()));
                    }
                }
        );
    }

    private void consumptionSubscriber(ConnectedNode connectedNode, String topicNameSuffix) {
        GraphName name = Settings.INSTANCE_NAME.join(topicNameSuffix);
        Subscriber<Float32> subscriber = connectedNode.newSubscriber(name, Float32._TYPE);
        subscriber.addMessageListener(
                new MessageListener<Float32>() {
                    @Override
                    public void onNewMessage(Float32 message) {
                        sendData("icns", format("%6.2f", message.getData()));
                    }
                }
        );
    }

    private void irSubscriber(ConnectedNode connectedNode, String topicNameSuffix) {
        GraphName name = Settings.INSTANCE_NAME.join(topicNameSuffix);
        Subscriber<Float32> subscriber = connectedNode.newSubscriber(name, Float32._TYPE);
        subscriber.addMessageListener(
                new MessageListener<Float32>() {
                    @Override
                    public void onNewMessage(Float32 message) {
                        sendData("proximity", format("%6.2f", message.getData()));
                    }
                }
        );
    }

    private String format(String formatString, Object... data) {
        return String.format(Locale.US, formatString, data);
    }

    private synchronized void sendData(String... nameData) {
        WebSocket socket = this.latestSocket;
        if (socket == null) return;
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (socket) {
            StringBuilder json = new StringBuilder("{");
            for (int i = 0; i < nameData.length; i += 2) {
                String name = nameData[i];
                String data = nameData[i + 1];
                if (i > 0) json.append(',');
                json.append('"').append(name).append("\":\"").append(data).append('"');
            }
            try {
                socket.send(json.append('}').toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void onShutdownComplete(Node node) {
        stop();
    }

    @Override
    public void onShutdown(Node node) {
    }

    @Override
    public void onError(Node node, Throwable throwable) {
    }

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("Java_simple_web_ctrl");
    }
}
