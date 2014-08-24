package elmot.javabrick.ev3;

import elmot.javabrick.ev3.impl.SensorFactory;

import java.io.IOException;

/**
 * @author elmot
 */
public class CompassSensorFactory extends SensorFactory {

    public int read(int daisyChainLevel, PORT port) throws IOException {
        return readRaw(daisyChainLevel, port)*2;
    }

    CompassSensorFactory(EV3 brick) {
        super(brick);
    }

}
