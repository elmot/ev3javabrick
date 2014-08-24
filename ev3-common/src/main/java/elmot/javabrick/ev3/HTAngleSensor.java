package elmot.javabrick.ev3;

import elmot.javabrick.ev3.impl.SensorFactory;

import java.io.IOException;

/**
 * @author elmot
 */
public class HTAngleSensor extends SensorFactory {
    public HTAngleSensor(EV3 brick) {
        super(brick);
    }

    public int readAngle(int daisyChainLevel, PORT port) throws IOException {
        int b = readRawByte(daisyChainLevel, port);
        if(b < 0) b = 256 + b;
        return b * 2;
    }
}
