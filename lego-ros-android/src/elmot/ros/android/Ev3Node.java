package elmot.ros.android;

import android.hardware.usb.UsbManager;
import elmot.javabrick.ev3.MotorFactory;
import elmot.javabrick.ev3.PORT;
import elmot.javabrick.ev3.android.usb.EV3BrickUsbAndroid;
import geometry_msgs.Point;
import nav_msgs.Odometry;
import org.jboss.netty.buffer.ChannelBuffer;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.DefaultSubscriberListener;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;
import std_msgs.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author elmot
 *         Date: 15.08.14
 */

//TODO odometry publisher

class Ev3Node extends AbstractNodeMain {

    private final UsbManager usbManager;
    private ConnectedNode connectedNode;
    private EV3BrickUsbAndroid brick;
    private Publisher<Float32> irPublisher;
    private Publisher<Float32> voltagePublisher;
    private Publisher<Odometry> odometryPublisher;

    Ev3Node(UsbManager usbManager) {
        this.usbManager = usbManager;
    }

    @Override
    public GraphName getDefaultNodeName() {
        return Settings.NODE_NAME.join("ev3");
    }

    @Override
    public void onStart(ConnectedNode connectedNode) {
        brick = new EV3BrickUsbAndroid(usbManager);
        clamp(false);
        super.onStart(connectedNode);
        this.connectedNode = connectedNode;
        irPublisher = connectedNode.newPublisher(Settings.NODE_NAME.join("ir_distance"), Float32._TYPE);
        voltagePublisher = connectedNode.newPublisher(Settings.NODE_NAME.join("voltage"), Float32._TYPE);
        odometryPublisher = connectedNode.newPublisher(Settings.NODE_NAME.join("odom"), Odometry._TYPE);

        Subscriber<Bool> clampSubscriber = connectedNode.newSubscriber(Settings.NODE_NAME.join("clamp"), Bool._TYPE);
        clampSubscriber.addMessageListener(new MessageListener<Bool>() {
            @Override
            public void onNewMessage(Bool bool) {
                clamp(bool.getData());
            }
        });

        Subscriber<ByteMultiArray> motorSpeedSubscriber = connectedNode.newSubscriber(Settings.NODE_NAME.join("motorSpeed"), ByteMultiArray._TYPE);
        motorSpeedSubscriber.addMessageListener(new MessageListener<ByteMultiArray>() {
            @Override
            public void onNewMessage(ByteMultiArray msg) {
                ChannelBuffer data = msg.getData();
                try {
                    brick.MOTOR.timeSync(0, MotorFactory.MOTORSET.BC, data.getByte(0), data.getByte(1), 1000, MotorFactory.BRAKE.COAST);
                } catch (IOException e) {
                    Ev3Node.this.connectedNode.getLog().error("Clamp error", e);
                }
            }
        });

        Subscriber<Int16> toneSubscriber = connectedNode.newSubscriber(Settings.NODE_NAME.join("tone"), Int16._TYPE);
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

        connectedNode.getScheduledExecutorService().scheduleAtFixedRate(new SensorSample(), Settings.SAMPLING_LOOP_MS, Settings.SAMPLING_LOOP_MS, TimeUnit.MILLISECONDS);

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
            connectedNode.getLog().error("Clamp error", e);
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

                Odometry odometry = odometryPublisher.newMessage();
                Header header = odometry.getHeader();
                header.setSeq(seq++);
                header.setFrameId("cat");
                header.setStamp(connectedNode.getCurrentTime());
                odometry.setChildFrameId("center");
                Point position = odometry.getPose().getPose().getPosition();
                position.setX(brick.MOTOR.getTacho(MotorFactory.MOTOR.B));
                position.setY(brick.MOTOR.getTacho(MotorFactory.MOTOR.C));
                odometryPublisher.publish(odometry);
            } catch (IOException e) {
                connectedNode.getLog().error("Sensors error", e);
            }
        }
    }
}
