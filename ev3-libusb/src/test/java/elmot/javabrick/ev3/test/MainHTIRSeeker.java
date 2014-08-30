package elmot.javabrick.ev3.test;

import elmot.javabrick.ev3.EV3;
import elmot.javabrick.ev3.HTIRSeeker;
import elmot.javabrick.ev3.PORT;
import org.junit.Ignore;
import org.junit.Test;

import javax.usb.UsbException;
import java.io.IOException;

/**
 * @author elmot
 */
public class MainHTIRSeeker extends TestBase{

    @Ignore
    @Test
    public void doTest() throws IOException, InterruptedException, UsbException {
        EV3 ev3 = findBrick();
        for (long startMs = System.currentTimeMillis(); System.currentTimeMillis() - startMs < 100000; ) {
            ev3.HT_IR_SEEKER.setMode(0,PORT.P4, HTIRSeeker.MODE.DIR_AC);
            int data = ev3.HT_IR_SEEKER.read(0, PORT.P4);
            System.out.println("data = " + data);

        }
    }
}
