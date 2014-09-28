package elmot.ros.android;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import elmot.javabrick.ev3.android.usb.EV3UsbAndroid;
import elmot.ros.ev3.Ev3Node;
import org.ros.namespace.GraphName;
import org.ros.node.DefaultNodeMainExecutor;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

/**
 * @author elmot
 *         Date: 13.08.14
 *         To change this template use File | Settings | File Templates.
 */


public class EV3NodeService extends Service {

    private Ev3Node ev3Node;
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
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        UsbManager usbManager = (UsbManager) getSystemService(USB_SERVICE);
        UsbDevice device = EV3UsbAndroid.findDevice(usbManager);
        if (device != null) {
            Notification notification = new Notification(R.drawable.ic_ev3_logo, "EV3 ROS Node", System.currentTimeMillis());
            notification.setLatestEventInfo(this, "EV3 ROS Node", "Started", null);
            startForeground(R.drawable.ic_ev3_logo, notification);
            ev3Node = new Ev3Node(new EV3UsbAndroid(usbManager),Settings.NODE_NAME.join("usb"),
                    GraphName.of(Settings.namespace(this)),Settings.SAMPLING_LOOP_MS, Settings.wheelRadius(this), Settings.wheelDist(this));
            NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(Settings.ownIpAddress(this));
            NodeMainExecutor nodeMainExecutor = DefaultNodeMainExecutor.newDefault();
            nodeMainExecutor.execute(ev3Node, nodeConfiguration);
        } else {
            stopSelf();
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        if (ev3Node != null) ev3Node.shutdown();
        if (wifiLock != null) wifiLock.release();
    }

}
