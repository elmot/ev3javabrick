package elmot.ros.android;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import elmot.ros.ev3.Settings;
import org.apache.commons.logging.LogFactory;
import org.ros.RosCore;

import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * @author elmot
 *         Date: 13.08.14
 */
public class RosMasterService extends Service {

    private RosCore rosCore;
    private WifiManager.WifiLock wifiLock;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiLock = wifiManager.createWifiLock("Ros master wifi lock");
        if (!wifiLock.isHeld()) {
            wifiLock.acquire();
        }
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
                rosCore = RosCore.newPublic(Settings.ownIpAddress(), 11311);
                rosCore.start();
            }
        }.start();
    }

    @Override
    public void onDestroy() {
        if (rosCore != null) rosCore.shutdown();
        if (wifiLock != null) wifiLock.release();
    }

}
