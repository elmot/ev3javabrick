package elmot.javabrick.ev3.libusb;

import elmot.javabrick.ev3.EV3;
import elmot.javabrick.ev3.EV3FactoryUsb;
import elmot.javabrick.ev3.PORT;
import org.junit.Ignore;
import org.junit.Test;

import javax.usb.UsbException;
import java.io.IOException;
import java.util.List;

/**
 * @author elmot
 */
public class MainHTIRSeeker {
    private EV3 findBrick() throws UsbException {
        List<EV3> ev3s = EV3FactoryUsb.listDiscovered();
        if (ev3s.isEmpty()) {
            throw new RuntimeException("No brick is found");
        }
        return ev3s.get(0);
    }

    @Ignore
    @Test
    public void doTest() throws IOException, InterruptedException, UsbException {
        EV3 ev3 = findBrick();
        for (long startMs = System.currentTimeMillis(); System.currentTimeMillis() - startMs < 100000; ) {
            ev3.HT_IR_SEEKER.setMode(0,PORT.P4,0);
            int data = ev3.HT_IR_SEEKER.read(0, PORT.P4);
            System.out.println("data = " + data);

        }
    }
}
