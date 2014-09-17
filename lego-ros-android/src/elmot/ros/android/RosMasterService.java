package elmot.ros.android;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.util.Log;
import elmot.ros.web.Server;
import org.apache.commons.logging.LogFactory;
import org.ros.RosCore;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * @author elmot
 *         Date: 13.08.14
 */
public class RosMasterService extends Service {

    private volatile RosCore rosCore;
    private volatile Server webServer;

    private volatile WifiManager.WifiLock wifiLock;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        String logName = Settings.NODE_NAME.toString();
        LogFactory.getLog(logName);
        LogManager logManager = LogManager.getLogManager();
        Logger logger = logManager.getLogger(logName);
        logger.setLevel(Level.ALL);
        if (Settings.needLocalMaster(this)) {
            Uri  uri = Uri.parse(Settings.masterUriAddress(RosMasterService.this));
            rosCore = RosCore.newPublic(uri.getHost(), uri.getPort());
        }
        if (Settings.needRunWeb(this)) {
            webServer = new Server(Settings.ownIpAddress(this), 8888);
        }

        if (webServer != null || rosCore != null) {
            WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            wifiLock = wifiManager.createWifiLock("Ros master wifi lock");
            if (!wifiLock.isHeld()) {
                wifiLock.acquire();
            }
            new Thread() {
                @Override
                public void run() {
                    int msgId = R.string.web_master_started;
                    if (rosCore != null) {
                        rosCore.start();
                    } else msgId = R.string.web_started;
                    if (webServer != null) {
                        try {
                            webServer.start();
                        } catch (IOException e) {
                            Log.e(Settings.LOG_TAG, "Webserver start error", e);
                            msgId = R.string.master_started;
                        }
                    } else msgId = R.string.master_started;
                    String startedMessage = getString(msgId);
                    Notification notification = new Notification(R.drawable.ic_ros_org, startedMessage, System.currentTimeMillis());
                    PendingIntent pendingIntent = PendingIntent.getActivity(RosMasterService.this, 0, new Intent(RosMasterService.this, RosMasterClosureActivity.class), Intent.FLAG_ACTIVITY_NEW_TASK);
                    notification.setLatestEventInfo(RosMasterService.this, startedMessage, getString(R.string.cick_to_stop), pendingIntent);
                    startForeground(R.drawable.ic_ros_org, notification);
                }
            }.start();
        } else
        {
            stopSelf();
        }
    }

    @Override
    public void onDestroy() {
        if (rosCore != null) rosCore.shutdown();
        if (webServer != null) webServer.stop();
        if (wifiLock != null) wifiLock.release();
    }

}
