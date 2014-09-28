package elmot.ros.ev3;

import org.ros.namespace.GraphName;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * @author elmot
 *         Date: 14.08.14
 *         To change this template use File | Settings | File Templates.
 */
public class Settings {
    public static final double WHEEL_DISTANCE_CM = 14.3;
    public static final double WHEEL_RADIUS_CM = 1.7;

    private Settings() {
    }

    /**
     * The facing of the camera is opposite to that of the screen.
     */
    @SuppressWarnings("UnusedDeclaration")
    public static final int CAMERA_FACING_BACK = 0;

    /**
     * The facing of the camera is the same as that of the screen.
     */

    public static final int CAMERA_FACING_FRONT = 1;

    public static final int CAMERA_FACING = CAMERA_FACING_FRONT;
//    public static final int CAMERA_FACING = Camera.CameraInfo.CAMERA_FACING_BACK;
    public static final int CAMERA_LOOP_MS = 2000;
    public static final int SAMPLING_LOOP_MS = 200;

    public static final String LOG_TAG = "ROS/ELMOT";

    public static final GraphName NODE_NAME = GraphName.of("/EV3_TEST");

    public static final GraphName INSTANCE_NAME = GraphName.of("/MINDSTORMS_A");

    public static String ownIpAddress() {
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
}
