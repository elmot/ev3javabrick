package elmot.javabrick.ev3.net;

import elmot.javabrick.ev3.EV3Brick;
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
        EV3Brick ev3Brick = EV3Base.openBlock();
        for (long startMs = System.currentTimeMillis(); System.currentTimeMillis() - startMs < 10000; ) {
            float value = ev3Brick.GYRO.read(0, PORT.P3, GyroSensorFactory.MODE.FAS);
//            float rate = ev3Brick.GYRO.read(0, PORT.P3, GyroSensorFactory.MODE.RATE);
//            float fas = ev3Brick.GYRO.read(0, PORT.P3, GyroSensorFactory.MODE.FAS);
//            System.out.printf("Angle = %f; Rate = %f; FAS= %f \n", angle, rate, fas);
            System.out.printf("V = %f\n", value);

        }
    }
}
