package elmot.ros.android;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
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
    public static final int SETTINGS_RESULT = 0;
    private ActivityNode activityNode;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (checkExit(getIntent())) return;
        setContentView(R.layout.main);
        runServices();
        SurfaceView cameraView = (SurfaceView) findViewById(R.id.cameraView);
        CameraPreview cameraPreview = CameraPreview.init(cameraView, Settings.cameraFacing(this));
        activityNode = new ActivityNode(cameraPreview);
        String host = Settings.ownIpAddress(this);
        NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(host);
        final NodeMainExecutor nodeMainExecutor = DefaultNodeMainExecutor.newDefault();
        nodeMainExecutor.execute(activityNode, nodeConfiguration);
        ((Chronometer) findViewById(R.id.chronometer)).start();
        String text = Settings.masterUriAddress(this);
        ((TextView) findViewById(R.id.masterUrlTextView)).setText(text);
    }

    private void runServices() {
        startService(masterIntent());
        startService(new Intent(this, EV3NodeService.class));
        if(Settings.bluetoothStart(this))
        {
            startService(new Intent(this,NXTBluetoothNodeService.class));
        }
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
            return GraphName.of(Settings.namespace(LegoRosActivity.this));
        }

        @Override
        public void onStart(final ConnectedNode connectedNode) {
            cameraPublisher = connectedNode.newPublisher(Settings.namespace(LegoRosActivity.this) + "/camera/compressed", CompressedImage._TYPE);
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
                text.setSpan(new ForegroundColorSpan(color), text.length() - stringBuilder.length(), text.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(R.string.options_menu).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                startActivityForResult(new Intent(LegoRosActivity.this, SettingsActivity.class), SETTINGS_RESULT);
                return true;
            }
        });
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == SETTINGS_RESULT) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    if (which == AlertDialog.BUTTON_POSITIVE) {
                        PendingIntent intent = PendingIntent.getActivity(getApplication().getBaseContext(), 0,
                                new Intent(getIntent()), getIntent().getFlags());
                        AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 2000, intent);
                        System.exit(2);
                    }
                }
            };
            builder.setTitle("Settings changed")
                    .setMessage("Restart to apply new settings?")
                    .setIcon(R.drawable.ic_ros_org)
                    .setPositiveButton("Yes", listener)
                    .setNegativeButton("Keep running", listener);
            builder.create().show();

        }
    }
}
