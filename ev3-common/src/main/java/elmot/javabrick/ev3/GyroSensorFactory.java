package elmot.javabrick.ev3;

import elmot.javabrick.ev3.impl.SensorFactory;

import java.io.IOException;

/**
 * @author elmot
 */
public class GyroSensorFactory extends SensorFactory {

    public void setMode(int daisyChainLevel, PORT port, MODE mode) throws IOException {
        setMode(daisyChainLevel, port, mode.val);
    }

    public enum MODE {
        ANGLE(0),
        RATE(1),
        FAS(2);

        private final int val;

        private MODE(int val) {
            this.val = val;
        }
    }

    GyroSensorFactory(EV3Brick brick) {
        super(brick);
    }

    public float read(int daisyChainLevel, PORT port, MODE mode) throws IOException {
        return readSI(daisyChainLevel, port, mode.val);
    }
}
