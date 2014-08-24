package elmot.javabrick.ev3.net;

import elmot.javabrick.ev3.EV3;
import elmot.javabrick.ev3.GyroSensorFactory;
import elmot.javabrick.ev3.PORT;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

/**
 * @author elmot
 */
public class MainGyro {
    @Ignore
    @Test
    public void doTest() throws IOException, InterruptedException {
        EV3 ev3 = EV3Base.openBlock();
        for (long startMs = System.currentTimeMillis(); System.currentTimeMillis() - startMs < 10000; ) {
            float value = ev3.GYRO.readSI(0, PORT.P3, GyroSensorFactory.MODE.FAS);
//            float rate = ev3.GYRO.read(0, PORT.P3, GyroSensorFactory.MODE.RATE);
//            float fas = ev3.GYRO.read(0, PORT.P3, GyroSensorFactory.MODE.FAS);
//            System.out.printf("Angle = %f; Rate = %f; FAS= %f \n", angle, rate, fas);
            System.out.printf("V = %f\n", value);

        }
    }
}
