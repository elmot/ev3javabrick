package elmot.javabrick.ev3;

import elmot.javabrick.ev3.impl.SensorFactory;

import java.io.IOException;

/**
 * @author elmot
 */
public class SoundSensorFactory extends SensorFactory {

    public float read(int daisyChainLevel, PORT port, SOUND_MODE mode) throws IOException {
        return getRead(daisyChainLevel, port, (byte) mode.val);
    }

    public enum SOUND_MODE {
        SOUND_DB(0),
        SOUND_DBA(1);

        private final int val;

        private SOUND_MODE(int val) {
            this.val = val;
        }
    }

    SoundSensorFactory(EV3 brick) {
        super(brick);
    }

}
