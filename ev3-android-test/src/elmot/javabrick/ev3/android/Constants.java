package elmot.javabrick.ev3.android;

/**
 * Created with IntelliJ IDEA.
 * User: elmot
 * Date: 03.08.14
 * Time: 13:45
 * To change this template use File | Settings | File Templates.
 */
public class Constants {
    public static final String LOG_TAG = "EV3/USB";
    public static final String ACTION_USB_DEVICE_ATTACHED = Constants.class.getPackage().getName() + ".ATTACHED";
    public static final String SERVICE_LOG = Constants.class.getPackage().getName() + ".DETACHED";

    public enum MsgSource {
        SERVICE(""), ACTIVITY("text-decoration:italic"), SYSTEM("text-weight:bold");
        public final String STYLE;

        private MsgSource(String STYLE) {
            this.STYLE = STYLE;
        }
    }
}
