package elmot.ros.android;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.IBinder;
import elmot.javabrick.ev3.android.usb.EV3BrickUsbAndroid;
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

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        UsbDevice device = EV3BrickUsbAndroid.findDevice((UsbManager) getSystemService(USB_SERVICE));
        if (device != null) {
            Notification notification = new Notification(R.drawable.ic_ev3_logo, "EV3 ROS Node", System.currentTimeMillis());
            notification.setLatestEventInfo(this, "EV3 ROS Node", "Started", null);
            startForeground(R.drawable.ic_ev3_logo, notification);
            ev3Node = new Ev3Node((UsbManager) getSystemService(USB_SERVICE));
            NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(Settings.ownIpAddress());
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
    }

}
