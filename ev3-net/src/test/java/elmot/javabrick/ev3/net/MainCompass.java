package elmot.javabrick.ev3.net;

import elmot.javabrick.ev3.EV3Brick;
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
        EV3Brick ev3Brick = EV3Base.openBlock();
        for (long startMs = System.currentTimeMillis(); System.currentTimeMillis() - startMs < 10000; ) {
            float data;

            data = ev3Brick.COMPASS.read(0, PORT.P4);
            System.out.println("Compass = " + data);

        }
    }
}
