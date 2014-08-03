package elmot.javabrick.ev3.net;

import elmot.javabrick.ev3.EV3Brick;
import jdk.nashorn.internal.ir.annotations.Ignore;
import org.junit.Test;

import java.io.IOException;

/**
 * @author elmot
 */
public class MainVoltage {
    @Ignore
    @Test
    public void doTest() throws IOException, InterruptedException {
        EV3Brick ev3Brick = EV3Base.openBlock();
        for (long startMs = System.currentTimeMillis(); System.currentTimeMillis() - startMs < 10000; ) {
            float vBatt = ev3Brick.SYSTEM.getVBatt();
            float iBatt = ev3Brick.SYSTEM.getIBatt();
            System.out.println("v/i Batt = " + vBatt + "/" + iBatt);
        }
    }
}
