package elmot.javabrick.ev3.net;

import elmot.javabrick.ev3.EV3;
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
        EV3 ev3 = EV3Base.openBlock();
        for (long startMs = System.currentTimeMillis(); System.currentTimeMillis() - startMs < 10000; ) {
            float vBatt = ev3.SYSTEM.getVBatt();
            float iBatt = ev3.SYSTEM.getIBatt();
            System.out.println("v/i Batt = " + vBatt + "/" + iBatt);
        }
    }
}
