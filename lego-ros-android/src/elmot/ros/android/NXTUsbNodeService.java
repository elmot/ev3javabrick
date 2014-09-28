package elmot.ros.android;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import elmot.javabrick.nxt.android.NXTUsbAndroid;
import elmot.ros.nxt.NXTNode;
import org.ros.namespace.GraphName;
import org.ros.node.DefaultNodeMainExecutor;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

/**
 * @author elmot
 *         Date: 13.08.14
 */


public class NXTUsbNodeService extends Service {

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
        wifiLock = wifiManager.createWifiLock("Ros NXT wifi lock");
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
            Notification.Builder notification = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.ic_nxt_logo)
                    .setTicker("NXT ROS Node")
                    .setWhen(System.currentTimeMillis())
                    .setContentTitle("NXT ROS Node")
                    .setContentText("Started");
            startForeground(R.drawable.ic_nxt_logo, notification.build());
            nxtNode = new NXTNode(new NXTUsbAndroid(usbManager), Settings.NODE_NAME.join("nxt"), GraphName.of(Settings.namespace(this)), Settings.SAMPLING_LOOP_MS);
            NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(Settings.ownIpAddress(NXTUsbNodeService.this));
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
