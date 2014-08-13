package elmot.javabrick.ev3.android;

import android.content.Context;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.util.Log;
import elmot.javabrick.ev3.android.usb.EV3BrickUsbAndroid;

import java.io.IOException;

public abstract class LegoTaskBase extends AsyncTask<Void, String, Exception> {
    private Ev3Activity ev3Activity;

    public LegoTaskBase(Ev3Activity ev3Activity) {
        this.ev3Activity = ev3Activity;
    }

    @Override
    protected Exception doInBackground(Void... devices) {
        try {
            EV3BrickUsbAndroid brick = new EV3BrickUsbAndroid((UsbManager) ev3Activity.getSystemService(Context.USB_SERVICE));
            runBrick(brick);
        } catch (RuntimeException e) {
            return e;
        } catch (IOException e) {
            return e;
        } catch (InterruptedException ignored) {
        }
        return null;
    }

    protected abstract void runBrick(EV3BrickUsbAndroid brick) throws IOException, InterruptedException;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        ev3Activity.updateButtons(true);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        afterStop();
    }

    @Override
    protected void onPostExecute(Exception exception) {
        super.onPostExecute(exception);
        if (exception != null)
            ev3Activity.log(Log.ERROR, Constants.MsgSource.SERVICE, exception.getClass().getName() + ": " + exception.getMessage());
        afterStop();
    }

    private void afterStop() {
        ev3Activity.log(Log.INFO, Constants.MsgSource.SERVICE, "Execution finished");
        ev3Activity.updateButtons(false);
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        ev3Activity.log(Log.INFO, Constants.MsgSource.SERVICE, values[0]);
    }

    protected void showProgress(String... messages) throws InterruptedException {
        checkRunning();
        publishProgress(messages);
    }

    private void checkRunning() throws InterruptedException {
        if (isCancelled()) {
            throw new InterruptedException();
        }
    }

    /**
     * @return read barcode value or null
     */
    protected String scanBarcode() {
        return ev3Activity.scanBarcode();
    }
}
