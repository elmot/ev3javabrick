package elmot.javabrick.ev3.android;

import android.hardware.Camera;
import com.google.zxing.DecodeHintType;

import java.util.HashMap;
import java.util.Map;

/**
 * @author  elmot
 * Date: 03.08.14
 */
public class Constants {
    public static final String LOG_TAG = "EV3/USB";
    public static final int CAMERA_FACING = Camera.CameraInfo.CAMERA_FACING_BACK;

    public static final Map<DecodeHintType, Object> BARCODE_HINTS;
    static {
        BARCODE_HINTS = new HashMap<DecodeHintType, Object>();
        BARCODE_HINTS.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        BARCODE_HINTS.put(DecodeHintType.ALLOWED_LENGTHS, new int[]{5, 6, 7, 8});
    }

    public enum MsgSource {
        SERVICE, ACTIVITY, SYSTEM
    }
}
