package elmot.javabrick.ev3;

import elmot.javabrick.ev3.impl.SensorFactory;

import java.io.IOException;

/**
 * @author elmot
 */
public class TempSensorFactory extends SensorFactory {

    public float read(int daisyChainLevel, PORT port) throws IOException {
        return readRaw(daisyChainLevel, port) / 256.0f;
    }

    TempSensorFactory(EV3 brick) {
        super(brick);
    }
}
