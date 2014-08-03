package elmot.javabrick.ev3.net;

import elmot.javabrick.ev3.EV3Brick;
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
        EV3Brick ev3Brick = EV3Base.openBlock();
        touch(ev3Brick);
    }

    private static void count(EV3Brick ev3Brick) throws IOException, InterruptedException {
        for (long startMs = System.currentTimeMillis(); System.currentTimeMillis() - startMs < 10000; ) {
            ev3Brick.TOUCH.clearChanges(0, PORT.P1);
            ev3Brick.TOUCH.setMode(0, PORT.P1, TouchSensorFactory.TOUCH_MODE.COUNT);
            for (int i = 0; i < 100; i++) {
                int bumps = ev3Brick.TOUCH.getBumps(0, PORT.P1);
                Thread.sleep(100);
                System.out.println("bumps = " + bumps);
            }
        }
    }

    private static void touch(EV3Brick ev3Brick) throws IOException, InterruptedException {
        ev3Brick.TOUCH.clearChanges(0, PORT.P1);
        ev3Brick.TOUCH.setMode(0, PORT.P1, TouchSensorFactory.TOUCH_MODE.BOOL);
        for (long startMs = System.currentTimeMillis(); System.currentTimeMillis() - startMs < 10000; ) {
            System.out.println("ev3Brick = " + ev3Brick.TOUCH.getTouch(0, PORT.P1));
        }
    }

}
