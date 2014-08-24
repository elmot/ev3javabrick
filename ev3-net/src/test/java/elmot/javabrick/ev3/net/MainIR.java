package elmot.javabrick.ev3.net;

import elmot.javabrick.ev3.EV3;
import elmot.javabrick.ev3.PORT;

import java.io.IOException;

import static elmot.javabrick.ev3.IRSensorFactory.IR_MODE;

import org.junit.Ignore;
import org.junit.Test;

/**
 * @author elmot
 */
public class MainIR {
    @Ignore
    @Test
    public void doTest() throws IOException, InterruptedException {
        EV3 ev3 = EV3Base.openBlock();
        ev3.SYSTEM.playTone(100, 1000, 1300);
        for (long startMs = System.currentTimeMillis(); System.currentTimeMillis() - startMs < 10000; ) {
            float data;

            ev3.IR.setMode(0, PORT.P3, IR_MODE.PROXIMITY);
            do {
                data = ev3.IR.readProximity(0, PORT.P3);
            }
            while (Float.isNaN(data));
            System.out.println("proximity = " + data);

//            Thread.sleep(500);

//            ev3.ULTRASONIC.setMode(0, PORT.P3, UltrasonicSensorFactory.ULTRASONIC_MODE.INCH);
//            while(Float.isNaN(data = ev3.ULTRASONIC.read(0, PORT.P3, UltrasonicSensorFactory.ULTRASONIC_MODE.INCH)));
//            System.out.println("inch = " + data);

//            Thread.sleep(500);

//            ev3.ULTRASONIC.setMode(0, PORT.P3, UltrasonicSensorFactory.ULTRASONIC_MODE.LISTEN);
//            while(Float.isNaN(data = ev3.ULTRASONIC.read(0, PORT.P3, UltrasonicSensorFactory.ULTRASONIC_MODE.LISTEN)));
//            System.out.println("listen = " + data);

//            Thread.sleep(500);
        }
    }
}
