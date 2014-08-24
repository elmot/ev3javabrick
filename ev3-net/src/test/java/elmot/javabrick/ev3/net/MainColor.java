package elmot.javabrick.ev3.net;

import elmot.javabrick.ev3.ColorSensorFactory;
import elmot.javabrick.ev3.EV3;
import elmot.javabrick.ev3.PORT;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

import static elmot.javabrick.ev3.ColorSensorFactory.COLOR;
import static elmot.javabrick.ev3.ColorSensorFactory.COLOR_MODE;

/**
 * @author elmot
 */
public class MainColor {
    @Ignore
    @Test
    public void doTest() throws IOException, InterruptedException {
        EV3 ev3 = EV3Base.openBlock();
        ev3.COLOR.setMode(0, PORT.P1, COLOR_MODE.NXT_BLUE);
        Thread.sleep(3000);
        ev3.COLOR.setMode(0, PORT.P1, ColorSensorFactory.COLOR_MODE.NXT_GREEN);
        Thread.sleep(3000);
/*
        ev3.COLOR.setMode(PORT.P1, COLOR_MODE.AMBIENT);
        for(int i = 0; i < 1000; i++)
        {
            System.out.println("Ambient: " + ev3.COLOR.getValue(PORT.P1));
        }
        ev3.COLOR.setMode(PORT.P1, COLOR_MODE.REFLECTION);
        for(int i = 0; i < 1000; i++)
        {
            System.out.println("REFLECTION: " + ev3.COLOR.getValue(PORT.P1));
        }
*/
        ev3.COLOR.setMode(0, PORT.P1, ColorSensorFactory.COLOR_MODE.COLOR);
        for (int i = 0; i < 1000; i++) {
            COLOR color = ev3.COLOR.getColor(PORT.P1);
            System.out.println("color = " + color);
        }
        ev3.COLOR.stopAll(0);
    }


}
