package elmot.ros.android;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import elmot.ros.android.hardware.CameraPreview;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ros.RosCore;
import org.ros.internal.node.server.master.MasterServer;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * @author elmot
 *         Date: 13.08.14
 */
public class RosMasterService extends Service {

    private RosCore rosCore;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Notification notification = new Notification(R.drawable.ic_ros_org, getString(R.string.master_started), System.currentTimeMillis());
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, RosMasterClosureActivity.class), Intent.FLAG_ACTIVITY_NEW_TASK);
        notification.setLatestEventInfo(this, getString(R.string.master_started), getString(R.string.cick_to_stop), pendingIntent);
        startForeground(R.drawable.ic_ros_org, notification);
        new Thread() {
            @Override
            public void run() {
                String logName = Settings.NODE_NAME.toString();
                LogFactory.getLog(logName);
                LogManager logManager = LogManager.getLogManager();
                Logger logger = logManager.getLogger(logName);
                logger.setLevel(Level.ALL);
                rosCore = RosCore.newPublic(Settings.ownIpAddress(),11311);
                rosCore.start();
            }
        }.start();
    }

    @Override
    public void onDestroy() {
        if (rosCore != null) rosCore.shutdown();
    }

}
