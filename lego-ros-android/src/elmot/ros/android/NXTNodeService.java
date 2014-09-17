package elmot.ros.android;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import elmot.javabrick.nxt.android.usb.NXTUsbAndroid;
import elmot.ros.nxt.NXTNode;
import org.ros.namespace.GraphName;
import org.ros.node.DefaultNodeMainExecutor;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

/**
 * @author elmot
 *         Date: 13.08.14
 */


public class NXTNodeService extends Service {

    private NXTNode nxtNode;
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
        UsbDevice device = NXTUsbAndroid.findDevice(usbManager);
        if (device != null) {
            Notification notification = new Notification(R.drawable.ic_ev3_logo, "EV3 ROS Node", System.currentTimeMillis());
            notification.setLatestEventInfo(this, "EV3 ROS Node", "Started", null);
            startForeground(R.drawable.ic_ev3_logo, notification);
            nxtNode = new NXTNode(new NXTUsbAndroid(usbManager), Settings.NODE_NAME.join("nxt"), GraphName.of(Settings.namespace(this)),Settings.SAMPLING_LOOP_MS);
            NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(Settings.ownIpAddress(NXTNodeService.this));
            NodeMainExecutor nodeMainExecutor = DefaultNodeMainExecutor.newDefault();
            nodeMainExecutor.execute(nxtNode, nodeConfiguration);
        } else {
            stopSelf();
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        if (nxtNode != null) nxtNode.shutdown();
        if (wifiLock != null) wifiLock.release();
    }

}
