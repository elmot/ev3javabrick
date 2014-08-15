package elmot.ros.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.SurfaceView;
import android.widget.Chronometer;
import android.widget.TextView;
import elmot.ros.android.hardware.CameraPreview;
import org.jboss.netty.buffer.LittleEndianHeapChannelBuffer;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.*;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;
import rosgraph_msgs.Log;
import sensor_msgs.CompressedImage;
import std_msgs.Header;

import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class LegoRosActivity extends Activity {


    public static final String EXIT_ACTION = LegoRosActivity.class.getPackage().getName() + ".EXIT";
    private ActivityNode activityNode;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (checkExit(getIntent())) return;
        setContentView(R.layout.main);
        runMasterServices();
        SurfaceView cameraView = (SurfaceView) findViewById(R.id.cameraView);
        CameraPreview cameraPreview = CameraPreview.init(cameraView);
        activityNode = new ActivityNode(cameraPreview);
        NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(Settings.ownIpAddress());
        final NodeMainExecutor nodeMainExecutor = DefaultNodeMainExecutor.newDefault();
        nodeMainExecutor.execute(activityNode, nodeConfiguration);
        ((Chronometer) findViewById(R.id.chronometer)).start();
        String text = "http://" + Settings.ownIpAddress() + ":11311/";
        ((TextView) findViewById(R.id.masterUrlTextView)).setText(text);

    }

    private void runMasterServices() {
        startService(masterIntent());
        startService(new Intent(this,EV3NodeService.class));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        checkExit(intent);
    }

    private boolean checkExit(Intent intent) {
        if (intent != null && EXIT_ACTION.equals(intent.getAction())) {
            finish();
            return true;
        }
        return false;
    }

    private Intent masterIntent() {
        return new Intent(this, RosMasterService.class);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (activityNode != null) activityNode.stopNode();
    }

    private class ActivityNode extends AbstractNodeMain implements Runnable {

        private CameraPreview cameraPreview;

        private Publisher<CompressedImage> cameraPublisher;
        private ScheduledFuture<?> scheduled;
        private ConnectedNode connectedNode;
        private int seq;

        private Deque<Log> logRecords = new LinkedList<Log>();

        private ActivityNode(CameraPreview cameraPreview) {
            this.cameraPreview = cameraPreview;
        }

        @Override
        public GraphName getDefaultNodeName() {
            return Settings.NODE_NAME;
        }

        @Override
        public void onStart(final ConnectedNode connectedNode) {
            cameraPublisher = connectedNode.newPublisher(Settings.INSTANCE_NAME + "/camera/compressed", CompressedImage._TYPE);
            this.connectedNode = connectedNode;
            scheduled = this.connectedNode.getScheduledExecutorService().scheduleAtFixedRate(this, Settings.CAMERA_LOOP_MS, Settings.CAMERA_LOOP_MS, TimeUnit.MILLISECONDS);
            Subscriber<Log> logger = connectedNode.newSubscriber("/rosout", Log._TYPE);
            logger.addMessageListener(new MessageListener<Log>() {
                final int startedSecs = connectedNode.getCurrentTime().secs;

                @Override
                public void onNewMessage(Log log) {
                    while (logRecords.size() >= Settings.MAX_LOG_RECORDS)
                        logRecords.removeLast();
                    logRecords.addFirst(log);
                    updateView(logRecords, startedSecs);
                }

            });
        }

        private void updateView(Collection<Log> logRecords1, int startedSecs) {
            final SpannableStringBuilder text = new SpannableStringBuilder();
            for (Log logRecord : logRecords1) {
                StringBuilder stringBuilder = new StringBuilder()
                        .append(logRecord.getHeader().getStamp().secs - startedSecs)
                        .append(logRecord.getHeader().getFrameId())
                        .append(": ").append(logRecord.getMsg());
                if (stringBuilder.length() > 200) stringBuilder.setLength(200);
                int color;
                if (logRecord.getLevel() <= Log.DEBUG)
                    color = 0xff000044;
                else if (logRecord.getLevel() <= Log.INFO)
                    color = 0xff004400;
                else if (logRecord.getLevel() <= Log.WARN)
                    color = 0xff994c00;
                else
                    color = 0xffe00000;
                text.append(stringBuilder);
                text.setSpan(new ForegroundColorSpan(color), text.length() - stringBuilder.length(), text.length() , Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                text.append("\n");
            }

            findViewById(R.id.logView).post(new Runnable() {
                @Override
                public void run() {
                    ((TextView) findViewById(R.id.logView)).setText(text);
                }
            });
        }
        @Override
        public void run() {
            try {
                byte[] cameraImage = cameraPreview.getCameraImage();
                if (cameraImage != null) {
                    CompressedImage msg = cameraPublisher.newMessage();
                    Header header = msg.getHeader();
                    header.setStamp(connectedNode.getCurrentTime());
                    header.setFrameId("SmartPhoneCamera");
                    header.setSeq(seq++);
                    msg.setFormat("JPEG");
                    msg.setData(new LittleEndianHeapChannelBuffer(cameraImage));
                    cameraPublisher.publish(msg);
                    connectedNode.getLog().debug("Image #" + msg.getHeader().getSeq());
                } else {
                    connectedNode.getLog().warn("Skip image. Preview destroyed?");
                }
            } catch (Exception e) {
                connectedNode.getLog().error("Can't publish image", e);
            }
        }


        public void stopNode() {
            scheduled.cancel(true);
            connectedNode.shutdown();
            cameraPreview.release();

        }
    }
}
