package elmot.ros.testmaster;

import elmot.ros.ev3.OdoComputer;
import geometry_msgs.TransformStamped;
import nav_msgs.Odometry;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;
import std_msgs.Float64;
import std_msgs.Header;

import java.util.concurrent.TimeUnit;

/**
 * @author elmot
 *         Date: 15.08.14
 */
class FakeOdo extends AbstractNodeMain {

    private volatile double dL = 3;
    private volatile double dR = 10;
    private volatile double l = 0;
    private volatile double r = 0;

    private OdoComputer odoComputer;
    private Publisher<Float64> lPubliser;
    private Publisher<Float64> rPubliser;
    private Publisher<Odometry> odomPublisher;
    private Publisher<TransformStamped> tfPublisher;

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("FakeOdo");
    }

    @Override
    public void onStart(final ConnectedNode connectedNode) {
        odoComputer = new OdoComputer(1.7, 14.3, connectedNode.getCurrentTime());
        diffSubscribers(connectedNode);
        movementExecutionSetup(connectedNode);
        odomPublisher = connectedNode.newPublisher("odom", Odometry._TYPE);
        tfPublisher = connectedNode.newPublisher("tf", TransformStamped._TYPE);
        connectedNode.getScheduledExecutorService().scheduleAtFixedRate(new Runnable() {
            private int seq = 0;

            @Override
            public void run() {
                synchronized (FakeOdo.this) {
                    seq++;
                    Odometry odometry = odomPublisher.newMessage();

                    Header header = odometry.getHeader();
                    TransformStamped transformStamped = tfPublisher.newMessage();

                    setupHeader(header);
                    setupHeader(transformStamped.getHeader());

                    transformStamped.setChildFrameId("base_footprint");
                    odometry.setChildFrameId("base_footprint");

                    odoComputer.computeOdometry((long) l, (long) r, odometry, transformStamped);
                    odomPublisher.publish(odometry);
//                    tfPublisher.publish(transformStamped);


                }
            }

            private void setupHeader(Header header) {
                header.setSeq(seq);
                header.setStamp(connectedNode.getCurrentTime());
                header.setFrameId("odom");
            }
        }, 50, 50, TimeUnit.MILLISECONDS);
    }

    private void movementExecutionSetup(ConnectedNode connectedNode) {
        lPubliser = connectedNode.newPublisher("L", Float64._TYPE);
        rPubliser = connectedNode.newPublisher("R", Float64._TYPE);
        connectedNode.getScheduledExecutorService().scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                synchronized (FakeOdo.this) {
                    l += dL;
                    r += dR;
                    publishDouble(lPubliser, l);
                    publishDouble(rPubliser, r);
                }
            }
        }, 100, 100, TimeUnit.MILLISECONDS);
    }

    private void publishDouble(Publisher<Float64> publisher, double v) {
        Float64 msg = publisher.newMessage();
        msg.setData(v);
        publisher.publish(msg);

    }

    private void diffSubscribers(ConnectedNode connectedNode) {
        super.onStart(connectedNode);
        Subscriber<Float64> subscriberL = connectedNode.newSubscriber("dL", Float64._TYPE);
        subscriberL.addMessageListener(new MessageListener<Float64>() {
            @Override
            public void onNewMessage(Float64 message) {
                synchronized (FakeOdo.this) {
                    dL = message.getData();
                }
            }
        });

        Subscriber<Float64> subscriberR = connectedNode.newSubscriber("dR", Float64._TYPE);
        subscriberR.addMessageListener(new MessageListener<Float64>() {
            @Override
            public void onNewMessage(Float64 message) {
                synchronized (FakeOdo.this) {
                    dR = message.getData();
                }
            }
        });
    }

}

