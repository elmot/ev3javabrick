package elmot.ros.nxt;

import elmot.javabrick.nxt.NXT;
import elmot.ros.ev3.OdoComputer;
import geometry_msgs.TransformStamped;
import geometry_msgs.Twist;
import nav_msgs.Odometry;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;
import std_msgs.Float32;
import std_msgs.Header;
import std_msgs.Int16;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author elmot
 *         Date: 15.08.14
 */

public class NXTNode extends AbstractNodeMain {

    private final GraphName nodeName;
    private final GraphName namespace;
    private ConnectedNode connectedNode;
    private NXT brick;
    private Publisher<Float32> ultasonicPublisher;
    private Publisher<Float32> voltagePublisher;
    private Publisher<Odometry> odometryPublisher;
    private Publisher<TransformStamped> tfPublisher;
    private OdoComputer odoComputer;
    private long loopMs;

    public NXTNode(NXT brick, GraphName nodeName, GraphName namespace, long loopMs) {
        this.brick = brick;
        this.nodeName = nodeName;
        this.namespace = namespace;
        this.loopMs = loopMs;
    }

    @Override
    public GraphName getDefaultNodeName() {
        return nodeName;
    }

    @Override
    public void onStart(ConnectedNode connectedNode) {
        this.connectedNode = connectedNode;
        super.onStart(connectedNode);

        odoComputer = new OdoComputer(1.7, 14.3, connectedNode.getCurrentTime());

        ultasonicPublisher = connectedNode.newPublisher(namespace.join("distance"), Float32._TYPE);
        voltagePublisher = connectedNode.newPublisher(namespace.join("voltage"), Float32._TYPE);
        odometryPublisher = connectedNode.newPublisher(namespace.join("odom"), Odometry._TYPE);
        tfPublisher = connectedNode.newPublisher(namespace.join("tf"), TransformStamped._TYPE);

        Subscriber<Twist> motorSpeedSubscriber = connectedNode.newSubscriber(namespace.join("cmd_vel"), Twist._TYPE);
        motorSpeedSubscriber.addMessageListener(new MessageListener<Twist>() {
            @Override
            public void onNewMessage(Twist msg) {
                double x = msg.getLinear().getX();
                double y = msg.getAngular().getZ();
                if (Math.abs(x) < 0.02) x = 0;
                if (Math.abs(y) < 0.02) y = 0;
                double s = x < 0 ? -1 : 1;
                double speed = Math.sqrt(x * x + y * y) / Math.sqrt(2);
                double twist = -200 * y / speed;
                speed *= 100 * s;
                try {
                    brick.MOTOR.steerMotors(NXT.Motor.A, NXT.Motor.B,  (int) speed, (int) twist, 10000);
                } catch (IOException e) {
                    NXTNode.this.connectedNode.getLog().error("Motor error", e);
                }
            }
        });

        Subscriber<Int16> toneSubscriber = connectedNode.newSubscriber(namespace.join("tone"), Int16._TYPE);
        toneSubscriber.addMessageListener(new MessageListener<Int16>() {
            @Override
            public void onNewMessage(Int16 freq) {
                try {
                    brick.SYSTEM.playTone(freq.getData(), 500);
                } catch (IOException e) {
                    NXTNode.this.connectedNode.getLog().error("Tone error", e);
                }

            }
        });

        connectedNode.getScheduledExecutorService().scheduleAtFixedRate(new SensorSample(), loopMs, loopMs, TimeUnit.MILLISECONDS);

    }

    public void shutdown() {
        connectedNode.shutdown();
    }


    private class SensorSample implements Runnable {
        private int seq = 0;

        @Override
        public void run() {
            try {
                Float32 proximity = ultasonicPublisher.newMessage();
                proximity.setData(brick.SENSOR.readUltrasonic(NXT.Sensor.P4));
                ultasonicPublisher.publish(proximity);

                Float32 voltage = voltagePublisher.newMessage();
                voltage.setData((float) brick.SYSTEM.getVBatt());
                voltagePublisher.publish(voltage);

                Odometry odometry = odometryPublisher.newMessage();
                setupHeader(odometry.getHeader(), seq);
                odometry.setChildFrameId("center");

                TransformStamped tfs = tfPublisher.newMessage();
                setupHeader(tfs.getHeader(), seq);
                tfs.setChildFrameId("center");

                long tachoL = brick.MOTOR.getAbsoluteTacho(NXT.Motor.A);
                long tachoR = brick.MOTOR.getAbsoluteTacho(NXT.Motor.B);
                odoComputer.computeOdometry(tachoL, tachoR, odometry, tfs);
                seq++;
                odometryPublisher.publish(odometry);
                tfPublisher.publish(tfs);
            } catch (IOException e) {
                connectedNode.getLog().error("Sensors error", e);
            }
        }

        private void setupHeader(Header header, int seq) {
            header.setSeq(seq);
            header.setFrameId("cat");
            header.setStamp(connectedNode.getCurrentTime());
        }
    }
}
