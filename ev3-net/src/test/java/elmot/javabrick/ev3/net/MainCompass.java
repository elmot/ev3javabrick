package elmot.javabrick.ev3.net;

import elmot.javabrick.ev3.EV3;
import elmot.javabrick.ev3.PORT;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

/**
 * @author elmot
 */
public class MainCompass {
    @Ignore
    @Test
    public void doTest() throws IOException, InterruptedException {
        EV3 ev3 = EV3Base.openBlock();
        for (long startMs = System.currentTimeMillis(); System.currentTimeMillis() - startMs < 10000; ) {
            float data;

            data = ev3.COMPASS.read(0, PORT.P4);
            System.out.println("Compass = " + data);

        }
    }
}
