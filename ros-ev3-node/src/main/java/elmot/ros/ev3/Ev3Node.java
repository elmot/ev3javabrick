package elmot.ros.ev3;

import elmot.javabrick.ev3.EV3;
import elmot.javabrick.ev3.MotorFactory;
import elmot.javabrick.ev3.PORT;
import geometry_msgs.TransformStamped;
import geometry_msgs.Twist;
import nav_msgs.Odometry;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;
import std_msgs.Bool;
import std_msgs.Float32;
import std_msgs.Header;
import std_msgs.Int16;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author elmot
 *         Date: 15.08.14
 */

public class Ev3Node extends AbstractNodeMain {

    private final long loopMs;
    private final double wheelRadiusCm;
    private final double wheelDistanceCm;
    private GraphName nodeName;
    private ConnectedNode connectedNode;
    private EV3 brick;
    private Publisher<Float32> irPublisher;
    private Publisher<Float32> voltagePublisher;
    private Publisher<Odometry> odometryPublisher;
    private Publisher<TransformStamped> tfPublisher;
    private OdoComputer odoComputer;
    private Publisher<Float32> consumptionPublisher;
    private GraphName namespace;

    public Ev3Node(EV3 brick,GraphName nodeName, GraphName namespace, long loopMs,
                   double wheelRadiusCm, double wheelDistanceCm) {
        this.brick = brick;
        this.nodeName = nodeName;
        this.namespace = namespace;
        this.loopMs = loopMs;
        this.wheelRadiusCm = wheelRadiusCm;
        this.wheelDistanceCm = wheelDistanceCm;
    }

    @Override
    public GraphName getDefaultNodeName() {
        return nodeName;
    }

    @Override
    public void onStart(ConnectedNode connectedNode) {
        this.connectedNode = connectedNode;
        super.onStart(connectedNode);

        clamp(false);

        odoComputer = new OdoComputer(wheelRadiusCm, wheelDistanceCm, connectedNode.getCurrentTime());

        irPublisher = connectedNode.newPublisher(namespace.join("ir_distance"), Float32._TYPE);
        voltagePublisher = connectedNode.newPublisher(namespace.join("voltage"), Float32._TYPE);
        consumptionPublisher = connectedNode.newPublisher(namespace.join("consumption"), Float32._TYPE);
        odometryPublisher = connectedNode.newPublisher(namespace.join("odom"), Odometry._TYPE);
        tfPublisher = connectedNode.newPublisher(namespace.join("tf"), TransformStamped._TYPE);

        Subscriber<Bool> clampSubscriber = connectedNode.newSubscriber(namespace.join("grip"), Bool._TYPE);
        clampSubscriber.addMessageListener(new MessageListener<Bool>() {
            @Override
            public void onNewMessage(Bool bool) {
                clamp(bool.getData());
            }
        });

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
                    brick.MOTOR.timeSync(0, MotorFactory.MOTORSET.BC, (int) speed, (int) twist, 1000, MotorFactory.BRAKE.COAST);
                } catch (IOException e) {
                    Ev3Node.this.connectedNode.getLog().error("Motor error", e);
                }
            }
        });

        Subscriber<Int16> toneSubscriber = connectedNode.newSubscriber(namespace.join("tone"), Int16._TYPE);
        toneSubscriber.addMessageListener(new MessageListener<Int16>() {
            @Override
            public void onNewMessage(Int16 freq) {
                try {
                    brick.SYSTEM.playTone(50, freq.getData(), 500);
                } catch (IOException e) {
                    Ev3Node.this.connectedNode.getLog().error("Tone error", e);
                }

            }
        });

        connectedNode.getScheduledExecutorService().scheduleAtFixedRate(new SensorSample(), loopMs, loopMs, TimeUnit.MILLISECONDS);

    }

    private void clamp(boolean b) {
        try {
            try {
                if (b) {
                    brick.MOTOR.powerTime(0, MotorFactory.MOTORSET.A, 100, 0, 700, 0, MotorFactory.BRAKE.BRAKE);

                } else {
                    brick.MOTOR.powerTime(0, MotorFactory.MOTORSET.A, -50, 0, 1500, 0, MotorFactory.BRAKE.COAST);
                }
                Thread.sleep(1500);
            } finally {
                brick.MOTOR.stop(MotorFactory.MOTORSET.A, MotorFactory.BRAKE.COAST);
            }
        } catch (Exception e) {
            connectedNode.getLog().error("Grip error", e);
        }
    }

    public void shutdown() {
        connectedNode.shutdown();
    }


    private class SensorSample implements Runnable {
        private int seq = 0;

        @Override
        public void run() {
            try {
                Float32 proximity = irPublisher.newMessage();
                proximity.setData(brick.IR.readProximity(0, PORT.P4));
                irPublisher.publish(proximity);

                Float32 voltage = voltagePublisher.newMessage();
                voltage.setData(brick.SYSTEM.getVBatt());
                voltagePublisher.publish(voltage);

                Float32 consumption = consumptionPublisher.newMessage();
                consumption.setData(brick.SYSTEM.getIBatt());
                consumptionPublisher.publish(consumption);

                Odometry odometry = odometryPublisher.newMessage();
                setupHeader(odometry.getHeader(), seq);
                odometry.setChildFrameId("center");

                TransformStamped tfs = tfPublisher.newMessage();
                setupHeader(tfs.getHeader(), seq);
                tfs.setChildFrameId("center");

                long tachoL = brick.MOTOR.getTacho(MotorFactory.MOTOR.B);
                long tachoR = brick.MOTOR.getTacho(MotorFactory.MOTOR.C);
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
