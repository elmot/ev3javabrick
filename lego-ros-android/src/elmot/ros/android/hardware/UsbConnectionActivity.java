package elmot.ros.android.hardware;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;
import elmot.ros.android.*;

/**
 * @author elmot
 *         Date: 14.08.14
 */
public class UsbConnectionActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean attached = UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(getIntent().getAction());
        setContentView(attached ? R.layout.usb_connected : R.layout.usb_disconnected);
        if (attached) {
            new RunAppTask().execute();
        } else {
            new StopUsbTask().execute();
        }

    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    private class StopUsbTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            UsbDevice device = getIntent().getParcelableExtra(UsbManager.EXTRA_DEVICE);
            startService(new Intent(UsbConnectionActivity.this, getNodeClass(device)));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            finish();
        }
    }

    private class RunAppTask extends AsyncTask<Void, String, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            UsbDevice device = getIntent().getParcelableExtra(UsbManager.EXTRA_DEVICE);
            Class nodeServiceClass = getNodeClass(device);
            UsbManager usbManager = (UsbManager) getSystemService(USB_SERVICE);
            UsbDeviceConnection usbDeviceConnection = usbManager.openDevice(device);
            try {
                final String message = "Serial: " + String.valueOf(usbDeviceConnection.getSerial()).toUpperCase();
                publishProgress(message);
            } finally {
                usbDeviceConnection.close();
            }
            Thread.yield();
            startService(new Intent(UsbConnectionActivity.this, RosMasterService.class));
            startService(new Intent(UsbConnectionActivity.this, nodeServiceClass));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
            startActivity(new Intent(UsbConnectionActivity.this, LegoRosActivity.class));
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            ((TextView) findViewById(R.id.serialField)).setText(values[0]);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            finish();
        }
    }

    private Class<? extends Service> getNodeClass(UsbDevice device) {
        return device.getProductId() == 5 ? EV3NodeService.class : NXTUsbNodeService.class;
    }

    /**
     * @author elmot
     *         Date: 14.08.14
     */
    public static class UsbDisconnectionReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            context.startActivity(new Intent(context, UsbConnectionActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION));
        }
    }
}