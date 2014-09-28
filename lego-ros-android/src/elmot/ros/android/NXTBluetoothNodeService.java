package elmot.ros.android;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import elmot.javabrick.nxt.android.NXTBluetoothAndroid;
import elmot.ros.nxt.NXTNode;
import org.ros.namespace.GraphName;
import org.ros.node.DefaultNodeMainExecutor;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

/**
 * @author elmot
 *         Date: 13.08.14
 */


public class NXTBluetoothNodeService extends Service {

    private NXTNode nxtNode;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        String bluetoothAddress = Settings.bluetoothAddress(this);
        if (bluetoothAddress == null) {
            Notification notification = newNotification(R.string.no_bt_device_title, R.string.no_bt_device_msg);
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(0, notification);
            stopSelf();
            return START_NOT_STICKY;
        }
        BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(bluetoothAddress);
        if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
            newNotification(R.string.no_bt_not_bonded_title, R.string.no_bt_not_bonded_msg);
            stopSelf();
            return START_NOT_STICKY;
        }
        Notification notification = newNotification(R.string.nxt_service_event_title, R.string.click_to_stop);
        startForeground(R.drawable.ic_nxt_logo, notification);
        nxtNode = new NXTNode(new NXTBluetoothAndroid(device), Settings.NODE_NAME.join("nxt"), GraphName.of(Settings.namespace(this)), Settings.SAMPLING_LOOP_MS);
        NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(Settings.ownIpAddress(NXTBluetoothNodeService.this));
        NodeMainExecutor nodeMainExecutor = DefaultNodeMainExecutor.newDefault();
        nodeMainExecutor.execute(nxtNode, nodeConfiguration);
        return START_NOT_STICKY;
    }

    private Notification newNotification(int eventTitleId, int contentId) {
        Notification.Builder notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_nxt_logo)
                .setTicker(getString(R.string.nxt_derice_notification_ticker))
                .setWhen(System.currentTimeMillis())
                .setContentTitle(getString(eventTitleId));
        if (contentId > 0) notification.setContentText(getString(contentId));
        return notification.build();
    }

    @Override
    public void onDestroy() {
        if (nxtNode != null) nxtNode.shutdown();
    }

}
