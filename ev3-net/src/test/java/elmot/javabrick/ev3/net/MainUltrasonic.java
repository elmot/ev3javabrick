package elmot.javabrick.ev3.net;

import elmot.javabrick.ev3.EV3Brick;
import elmot.javabrick.ev3.PORT;

import java.io.IOException;

import static elmot.javabrick.ev3.UltrasonicSensorFactory.ULTRASONIC_MODE;

import org.junit.Ignore;
import org.junit.Test;

/**
 * @author elmot
 */
public class MainUltrasonic {
    @Ignore
    @Test
    public void doTest() throws IOException, InterruptedException {
        EV3Brick ev3Brick = EV3Base.openBlock();
        for (long startMs = System.currentTimeMillis(); System.currentTimeMillis() - startMs < 10000; ) {
            float data;

            ev3Brick.ULTRASONIC.setMode(0, PORT.P3, ULTRASONIC_MODE.CM);
            data = ev3Brick.ULTRASONIC.read(0, PORT.P3);
            System.out.println("cm = " + data);

        }
    }
}
