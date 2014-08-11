package elmot.javabrick.ev3_ros_node;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;

public class MasterNode extends Service {


    @Override
    public IBinder onBind(Intent intent) {
        return new Callback();
    }

    private class Callback extends Binder {
        @Override
        protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == IBinder.LAST_CALL_TRANSACTION) {
                stop();
                return true;
            }
            return super.onTransact(code, data, reply, flags);
        }
    }

    private void stop() {
        stopSelf();
    }
}
