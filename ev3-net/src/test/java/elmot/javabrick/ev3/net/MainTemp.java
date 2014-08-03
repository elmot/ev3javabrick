package elmot.javabrick.ev3.net;

import elmot.javabrick.ev3.EV3Brick;
import elmot.javabrick.ev3.PORT;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

/**
 * @author elmot
 */
public class MainTemp {
    @Ignore
    @Test
    public void doTest() throws IOException, InterruptedException {
        EV3Brick ev3Brick = EV3Base.openBlock();
        for (long startMs = System.currentTimeMillis(); System.currentTimeMillis() - startMs < 10000; ) {
            float data;

            data = ev3Brick.TEMP.read(0, PORT.P3);
            System.out.println("T(C) = " + data);

        }
    }
}
