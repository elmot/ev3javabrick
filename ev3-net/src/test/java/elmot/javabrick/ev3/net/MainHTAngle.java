package elmot.javabrick.ev3.net;

import elmot.javabrick.ev3.EV3;
import elmot.javabrick.ev3.PORT;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

/**
 * @author elmot
 */
public class MainHTAngle {
    @Ignore
    @Test
    public void doTest() throws IOException, InterruptedException {
        EV3 ev3 = EV3Base.openBlock();
        for (long startMs = System.currentTimeMillis(); System.currentTimeMillis() - startMs < 10000; ) {
            int data = ev3.HT_ANGLE.readAngle(0, PORT.P3);
            System.out.println("data = " + data);

        }
    }
}
