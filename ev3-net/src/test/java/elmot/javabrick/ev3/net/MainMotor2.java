package elmot.javabrick.ev3.net;

import elmot.javabrick.ev3.EV3;

import java.io.IOException;

import static elmot.javabrick.ev3.MotorFactory.BRAKE;
import static elmot.javabrick.ev3.MotorFactory.MOTORSET;

import org.junit.Ignore;
import org.junit.Test;

/**
 * @author elmot
 */
public class MainMotor2 {
    @Ignore
    @Test
    public void doTest() throws IOException, InterruptedException {
        EV3 ev3 = EV3Base.openBlock();

        ev3.SYSTEM.playTone(30, 200, 400);
        Thread.sleep(400);

        ev3.MOTOR.powerStep(0, MOTORSET.C, -70, 100, 5000, 100, BRAKE.COAST);
        ev3.MOTOR.waitForCompletion(MOTORSET.C);

        ev3.SYSTEM.playTone(30, 400, 400);
        Thread.sleep(400);

        ev3.MOTOR.powerTime(0, MOTORSET.C, +60, 500, 700, 400, BRAKE.BRAKE);
        ev3.MOTOR.waitForCompletion(MOTORSET.C);

        ev3.SYSTEM.playTone(30, 800, 400);
        Thread.sleep(400);

        ev3.MOTOR.speedStep(0, MOTORSET.C, -70, 100, 5000, 100, BRAKE.COAST);
        ev3.MOTOR.waitForCompletion(MOTORSET.C);

        ev3.SYSTEM.playTone(30, 400, 400);
        Thread.sleep(400);

        ev3.MOTOR.speedTime(0, MOTORSET.C, +60, 500, 700, 400, BRAKE.BRAKE);
        ev3.MOTOR.waitForCompletion(0, MOTORSET.C);

        ev3.SYSTEM.playTone(30, 800, 400);
        Thread.sleep(400);

        ev3.MOTOR.stepSync(0, MOTORSET.AC, +60, 130, 1300, BRAKE.BRAKE);
        ev3.MOTOR.waitForCompletion(MOTORSET.AC);


        ev3.SYSTEM.playTone(30, 1600, 400);
        Thread.sleep(400);

        ev3.MOTOR.stepSync(0, MOTORSET.AC, -60, 200, 1300, BRAKE.BRAKE);

    }

}
