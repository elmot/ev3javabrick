package elmot.javabrick.ev3.net;

import elmot.javabrick.ev3.EV3;
import elmot.javabrick.ev3.PORT;
import elmot.javabrick.ev3.TouchSensorFactory;

import java.io.IOException;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author elmot
 */
public class MainTouch {
    @Ignore
    @Test
    public void doTest() throws IOException, InterruptedException {
        EV3 ev3 = EV3Base.openBlock();
        touch(ev3);
    }

    private static void count(EV3 ev3) throws IOException, InterruptedException {
        for (long startMs = System.currentTimeMillis(); System.currentTimeMillis() - startMs < 10000; ) {
            ev3.TOUCH.clearChanges(0, PORT.P1);
            ev3.TOUCH.setMode(0, PORT.P1, TouchSensorFactory.TOUCH_MODE.COUNT);
            for (int i = 0; i < 100; i++) {
                int bumps = ev3.TOUCH.getBumps(0, PORT.P1);
                Thread.sleep(100);
                System.out.println("bumps = " + bumps);
            }
        }
    }

    private static void touch(EV3 ev3) throws IOException, InterruptedException {
        ev3.TOUCH.clearChanges(0, PORT.P1);
        ev3.TOUCH.setMode(0, PORT.P1, TouchSensorFactory.TOUCH_MODE.BOOL);
        for (long startMs = System.currentTimeMillis(); System.currentTimeMillis() - startMs < 10000; ) {
            System.out.println("ev3 = " + ev3.TOUCH.getTouch(0, PORT.P1));
        }
    }

}
