package elmot.javabrick.ev3.net;


import elmot.javabrick.ev3.ColorSensorFactory;
import elmot.javabrick.ev3.EV3;
import elmot.javabrick.ev3.MotorFactory;
import elmot.javabrick.ev3.PORT;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

import static elmot.javabrick.ev3.MotorFactory.*;

/**
 * @author elmot
 */
public class MainMotor {
    @Ignore
    @Test
    public void doTest() throws IOException, InterruptedException {
        EV3 ev3 = EV3Base.openBlock();

        ev3.COLOR.setMode(0, PORT.P1, ColorSensorFactory.COLOR_MODE.COLOR);
        ColorSensorFactory.COLOR color = ev3.COLOR.getColor(PORT.P1);
        System.out.println("color = " + color);

        ev3.MOTOR.speed(MOTORSET.AB, -30);
        ev3.MOTOR.start(MOTORSET.AB);
        Thread.sleep(3000);
        ev3.MOTOR.speed(MOTORSET.A, 40);
        Thread.sleep(3000);
        ev3.MOTOR.direction(MOTORSET.A, MotorFactory.DIR.TOGGLE);
        Thread.sleep(3000);
        ev3.MOTOR.stop(MOTORSET.ABCD, BRAKE.COAST);
        System.out.println("ev3.MOTOR.getTacho(A) = " + ev3.MOTOR.getTacho(MOTOR.A));
        System.out.println("ev3.MOTOR.getTacho(B) = " + ev3.MOTOR.getTacho(MOTOR.B));
        ev3.MOTOR.resetTacho(MOTORSET.A);
        System.out.println("ev3.MOTOR.getTacho(A) = " + ev3.MOTOR.getTacho(MOTOR.A));
        System.out.println("ev3.MOTOR.getTacho(B) = " + ev3.MOTOR.getTacho(MOTOR.B));
    }

}
