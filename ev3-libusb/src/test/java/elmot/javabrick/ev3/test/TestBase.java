package elmot.javabrick.ev3.test;

import elmot.javabrick.ev3.EV3;
import elmot.javabrick.ev3.EV3FactoryUsb;

import javax.usb.UsbException;
import java.io.IOException;
import java.util.List;

/**
 * @author elmot
 *         Date: 30.08.14
 */
public class TestBase {
    protected static EV3 findBrick() throws IOException {
        List<EV3> ev3s = null;
        try {
            ev3s = EV3FactoryUsb.listDiscovered();
        } catch (UsbException e) {
            throw new IOException(e);
        }
        if (ev3s.isEmpty()) {
            throw new RuntimeException("No brick is found");
        }
        return ev3s.get(0);
    }
}
