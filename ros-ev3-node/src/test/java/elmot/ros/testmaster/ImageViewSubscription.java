package elmot.ros.testmaster;

import org.jboss.netty.buffer.ChannelBuffer;
import org.ros.internal.node.topic.PublisherIdentifier;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.*;
import org.ros.node.topic.Subscriber;
import org.ros.node.topic.SubscriberListener;
import sensor_msgs.CompressedImage;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;

public class ImageViewSubscription extends AbstractNodeMain implements MessageListener<CompressedImage>, SubscriberListener<CompressedImage> {

    private static JFrame jFrame;
    private static JLabel toDraw;

    public static void main(String[] args) {
        final NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic("192.168.1.41", URI.create("http://192.168.1.37:11311/"));
//        final NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(HOST, rosCore.getUri());
        final NodeMainExecutor nodeMainExecutor = DefaultNodeMainExecutor.newDefault();
        ImageViewSubscription imageViewSubscription = new ImageViewSubscription();
        nodeMainExecutor.execute(imageViewSubscription, nodeConfiguration);

    }

    @Override
    public void onStart(ConnectedNode connectedNode) {
        runSubscription(connectedNode,"/MINDSTORMS_A/camera/compressed");
    }

    public static void runSubscription(ConnectedNode node, String topic) {
        Subscriber<CompressedImage> objectSubscriber = node.newSubscriber(topic, CompressedImage._TYPE);
        ImageViewSubscription imageViewSubscription = new ImageViewSubscription();
        objectSubscriber.addMessageListener(imageViewSubscription);
        objectSubscriber.addSubscriberListener(imageViewSubscription);
        node.getLog().info("CompressedImage Subscriber started: " + node.getName() + ":" + topic);
        jFrame = new JFrame("CompressedImage Subscriber: " + node.getName() + ":" + topic);
        jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jFrame.setLayout(null);
        toDraw = new JLabel();
        toDraw.setLocation(0, 0);
        jFrame.add(toDraw);
        jFrame.setVisible(true);
    }

    @Override
    public void onNewMessage(CompressedImage message) {
        try {
            ChannelBuffer data = message.getData();
            byte[] bytes = new byte[data.readableBytes()];
            data.getBytes(0, bytes);
            BufferedImage received = ImageIO.read(new ByteArrayInputStream(bytes));
            Graphics graphics = received.getGraphics();
            try {
                graphics.drawString("Frame:" + message.getHeader().getSeq(), 30, 30);
            } finally {
                graphics.dispose();
            }
            toDraw.setIcon(new ImageIcon(received));
            toDraw.setSize(received.getWidth(), received.getHeight());
            jFrame.setSize(toDraw.getSize());
        } catch (IOException ignored) {

        }

    }

    @Override
    public void onShutdown(Subscriber<CompressedImage> subscriber) {
        if (subscriber == this) {
            jFrame.dispose();
        }
    }

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("Java_image_viewer");
    }

    @Override
    public void onNewPublisher(Subscriber<CompressedImage> subscriber, PublisherIdentifier publisherIdentifier) {
    }

    @Override
    public void onMasterRegistrationSuccess(Subscriber<CompressedImage> registrant) {
    }

    @Override
    public void onMasterRegistrationFailure(Subscriber<CompressedImage> registrant) {
    }

    @Override
    public void onMasterUnregistrationSuccess(Subscriber<CompressedImage> registrant) {
    }

    @Override
    public void onMasterUnregistrationFailure(Subscriber<CompressedImage> registrant) {
    }
}
