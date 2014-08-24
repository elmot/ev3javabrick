package elmot.javabrick.ev3;

import elmot.javabrick.ev3.impl.SensorFactory;

import java.io.IOException;

/**
 * @author elmot
 */
public class CommonSensorFactory extends SensorFactory {

    public int read(int daisyChainLevel, PORT port) throws IOException {
        return readRaw(daisyChainLevel, port);
    }

    CommonSensorFactory(EV3 brick) {
        super(brick);
    }

}
