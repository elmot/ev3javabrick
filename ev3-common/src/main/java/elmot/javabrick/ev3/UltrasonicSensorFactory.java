package elmot.javabrick.ev3;

import elmot.javabrick.ev3.impl.SensorFactory;

import java.io.IOException;

/**
 * @author elmot
 */
public class UltrasonicSensorFactory extends SensorFactory {

    public void setMode(int daisyChainLevel, PORT port, ULTRASONIC_MODE mode) throws IOException {
        setMode(daisyChainLevel, port, mode.val);
    }

    public float read(int daisyChainLevel, PORT port) throws IOException {
        return readRaw(daisyChainLevel, port);
    }

    public enum ULTRASONIC_MODE {
        CM(0),
        INCH(1),
        LISTEN(2);

        private final int val;

        private ULTRASONIC_MODE(int val) {
            this.val = val;
        }
    }

    UltrasonicSensorFactory(EV3 brick) {
        super(brick);
    }

    public float readSi(int daisyChainLevel, PORT port, ULTRASONIC_MODE mode) throws IOException {
        return readSI(daisyChainLevel, port, mode.val);
    }
}
