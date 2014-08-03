package elmot.javabrick.ev3.net;


import elmot.javabrick.ev3.ColorSensorFactory;
import elmot.javabrick.ev3.EV3Brick;
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
        EV3Brick ev3Brick = EV3Base.openBlock();

        ev3Brick.COLOR.setMode(0, PORT.P1, ColorSensorFactory.COLOR_MODE.COLOR);
        ColorSensorFactory.COLOR color = ev3Brick.COLOR.getColor(PORT.P1);
        System.out.println("color = " + color);

        ev3Brick.MOTOR.speed(MOTORSET.AB, -30);
        ev3Brick.MOTOR.start(MOTORSET.AB);
        Thread.sleep(3000);
        ev3Brick.MOTOR.speed(MOTORSET.A, 40);
        Thread.sleep(3000);
        ev3Brick.MOTOR.direction(MOTORSET.A, MotorFactory.DIR.TOGGLE);
        Thread.sleep(3000);
        ev3Brick.MOTOR.stop(MOTORSET.ABCD, BRAKE.COAST);
        System.out.println("ev3Brick.MOTOR.getTacho(A) = " + ev3Brick.MOTOR.getTacho(MOTOR.A));
        System.out.println("ev3Brick.MOTOR.getTacho(B) = " + ev3Brick.MOTOR.getTacho(MOTOR.B));
        ev3Brick.MOTOR.resetTacho(MOTORSET.A);
        System.out.println("ev3Brick.MOTOR.getTacho(A) = " + ev3Brick.MOTOR.getTacho(MOTOR.A));
        System.out.println("ev3Brick.MOTOR.getTacho(B) = " + ev3Brick.MOTOR.getTacho(MOTOR.B));
    }

}
