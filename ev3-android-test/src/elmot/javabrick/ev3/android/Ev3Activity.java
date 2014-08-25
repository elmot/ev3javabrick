package elmot.javabrick.ev3.android;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.example.ev3_android_test.R;
import com.google.zxing.Result;
import elmot.javabrick.barcode.CameraPreview;

import java.util.Deque;
import java.util.LinkedList;

import static elmot.javabrick.ev3.android.Constants.MsgSource;

public class Ev3Activity extends Activity {
    BroadcastReceiver detachReceiver;
    private TextView logView;
    private Button runButton;
    private Button stopButton;
    private Chronometer chronometer;
    private LegoTaskBase runTask = null;
    private Deque<Spannable> logEnties = new LinkedList<Spannable>();
    private CameraPreview cameraPreview;

    public void log(int level, MsgSource source, String message) {
        Log.println(level, "EV3/USB", message == null ? "NULL" : message);
        Spannable spannable = Spannable.Factory.getInstance().newSpannable(source + ": " + message);
        int color = Color.WHITE;
        switch (level) {
            case Log.INFO:
                color = Color.BLUE;
                break;
            case Log.WARN:
                color = Color.YELLOW;
                break;
            case Log.ERROR:
                color = Color.RED;
                break;
            case Log.DEBUG:
                color = Color.GREEN;
                break;
        }
        spannable.setSpan(new ForegroundColorSpan(color), 0, spannable.length(), 0);

        logEnties.addFirst(spannable);
        while (logEnties.size() > 50) {
            logEnties.removeLast();
        }

        SpannableStringBuilder text = new SpannableStringBuilder();
        for (Spannable logEntry : logEnties) {
            text.append(logEntry).append('\n');
        }
        logView.setText(text);
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
        logView = (TextView) findViewById(R.id.logView);
        runButton = (Button) findViewById(R.id.runButton);
        stopButton = (Button) findViewById(R.id.stopButton);
        chronometer = (Chronometer) findViewById(R.id.chronometer);
        log(Log.INFO, MsgSource.ACTIVITY, "Started");
        IntentFilter filter = new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED);
        detachReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                String name = usbDevice.getDeviceName();
                log(Log.INFO, MsgSource.SYSTEM, "Device detached: " + name);
                updateButtons(Ev3Activity.this.isRunning());
            }
        };
        registerReceiver(detachReceiver, filter);

        runButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runTask();
            }
        });
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                log(Log.WARN, MsgSource.ACTIVITY, "Stopping");
                runTask.cancel(true);
                stopButton.setPressed(true);
                for (int i = 0; Ev3Activity.this.isRunning() && i < 100; i++) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        updateButtons(false);
                    }
                }
            }
        });
        // Create an instance of Camera

        // Create our Preview view and set it as the content of our activity.
        cameraPreview = new CameraPreview(this);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(cameraPreview);

        if (isAutoStart())
            runTask();
    }

    public boolean isAutoStart() {
        return true;
    }

    private void runTask() {
        runTask = new LegoTask(this);
        runTask.execute();
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();
        updateButtons(this.isRunning());
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();
        if (intent != null) {
            String action = intent.getAction();
            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                log(Log.INFO, MsgSource.SYSTEM, "Device attached: " + usbDevice.getDeviceName());
                updateButtons(isRunning());
            }
        }

    }

    public void updateButtons(boolean running) {
        stopButton.setPressed(false);
        if (running) {
            stopButton.setEnabled(true);
            runButton.setEnabled(false);
            chronometer.stop();
        } else {
            stopButton.setEnabled(false);
            runButton.setEnabled(true);
        }
    }

    private boolean isRunning() {
        return runTask != null &&
                runTask.getStatus() == AsyncTask.Status.RUNNING;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(detachReceiver);
    }

    public Result getLastDecodedBarcode() {
        return cameraPreview.getLastDecoded();
    }

    public Result scanPreciseBarcode() {
        return cameraPreview.scanPreciseBarcode();
    }


}
