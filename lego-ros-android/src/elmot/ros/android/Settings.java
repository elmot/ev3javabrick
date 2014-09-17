package elmot.ros.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import org.ros.namespace.GraphName;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * @author elmot
 *         Date: 15.09.14
 */
public class Settings {
    //todo refactor
    public static final GraphName NODE_NAME = GraphName.of("/EV3_TEST");
    public static final int CAMERA_LOOP_MS = 2000;
    public static final int SAMPLING_LOOP_MS = 200;
    public static final int MAX_LOG_RECORDS = 50;
    public static final String LOG_TAG = "ROS/ELMOT";

    private static String findRealOwnIp() {
        try {
            for (Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces(); interfaces.hasMoreElements(); ) {
                NetworkInterface anInterface = interfaces.nextElement();
                if (anInterface.isUp() && !anInterface.isVirtual() && !anInterface.isLoopback())
                    for (Enumeration<InetAddress> inetAddresses = anInterface.getInetAddresses(); inetAddresses.hasMoreElements(); ) {
                        InetAddress inetAddress = inetAddresses.nextElement();
                        if (!inetAddress.isLoopbackAddress() && !inetAddress.isMulticastAddress() && inetAddress.getAddress().length == 4)
                            return inetAddress.getHostAddress();
                    }
            }
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public static String ownIpAddress(Context context) {
        SharedPreferences preferences = getPreferences(context);
        boolean autoIp = getBoolean(context, preferences, R.string.auto_own_ip, R.string.def_auto_own_ip);
        if (autoIp) return findRealOwnIp();
        return getPreferences(context).getString(context.getString(R.string.own_ip), findRealOwnIp());
    }

    private static boolean getBoolean(Context context, SharedPreferences preferences, int keyId, int defId) {
        return preferences.getBoolean(context.getString(keyId), Boolean.valueOf(context.getString(defId)));
    }

    public static String masterUriAddress(Context context) {
        SharedPreferences preferences = getPreferences(context);
        boolean localMaster = needLocalMaster(context, preferences);
        if (localMaster) return "http://" + ownIpAddress(context) +":11311/";
        return getPreferences(context).getString(context.getString(R.string.ext_master_url), context.getString(R.string.def_ext_master_url));
    }

    private static boolean needLocalMaster(Context context, SharedPreferences preferences) {
        return getBoolean(context, preferences, R.string.local_master, R.string.def_local_master);
    }

    public static boolean needLocalMaster(Context context) {
        return getBoolean(context, getPreferences(context), R.string.local_master, R.string.def_local_master);
    }

    public static boolean needRunWeb(Context context) {
        return getBoolean(context, getPreferences(context), R.string.local_webserver, R.string.def_local_webserver);
    }

    public static SharedPreferences getPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static String namespace(Context context) {
        return getString(context, R.string.namespace, R.string.def_namespace);
    }

    private static String getString(Context context, int keyId, int defId) {
        return getPreferences(context).getString(context.getString(keyId), context.getString(defId));
    }

    public static double wheelRevolutionTrackLength(Context context) {
        return Double.valueOf(getString(context, R.string.wheel_turn_cm, R.string.def_wheel_turn_cm));
    }

    public static double wheelDist(Context context) {
        return Double.valueOf(getString(context, R.string.wheel_dist_cm, R.string.def_wheel_dist_cm));
    }

    public static int cameraFacing(Context context) {
        return Integer.valueOf(getString(context, R.string.camera_side, R.string.def_camera_side));
    }

}
