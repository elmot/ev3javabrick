package elmot.javabrick.ev3.libusb;

import elmot.javabrick.ev3.EV3Brick;
import elmot.javabrick.ev3.EV3FactoryUsb;
import org.junit.Ignore;
import org.junit.Test;

import javax.usb.UsbException;
import java.io.IOException;
import java.util.List;

/**
 * @author elmot
 */
public class MainVoltageUsb {
    @Ignore
    @Test
    public void testSoundVolts() throws IOException, InterruptedException, UsbException {
        List<EV3Brick> ev3Bricks = EV3FactoryUsb.listDiscovered();
        if(ev3Bricks.isEmpty())
        {
            throw new RuntimeException("No brick is found");
        }
        EV3Brick ev3Brick = ev3Bricks.get(0);
        ev3Brick.SYSTEM.playTone(100,880,700);
        for (long startMs = System.currentTimeMillis(); System.currentTimeMillis() - startMs < 10000;)
        {
            float vBatt = ev3Brick.SYSTEM.getVBatt();
            float iBatt = ev3Brick.SYSTEM.getIBatt();
            System.out.println("v/i Batt = " + vBatt + "/" + iBatt);
        }
    }
}
