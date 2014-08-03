package elmot.javabrick.ev3.net;

import elmot.javabrick.ev3.EV3Brick;
import elmot.javabrick.ev3.PORT;
import elmot.javabrick.ev3.SoundSensorFactory;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

/**
 * @author elmot
 */
public class MainSound {
    @Ignore
    @Test
    public void doTest() throws IOException, InterruptedException {
        EV3Brick ev3Brick = EV3Base.openBlock();
        for (long startMs = System.currentTimeMillis(); System.currentTimeMillis() - startMs < 10000; ) {
            float data;

            data = ev3Brick.SOUND.read(0, PORT.P3, SoundSensorFactory.SOUND_MODE.SOUND_DBA);
            System.out.println("db(a) = " + data);

        }
    }
}
