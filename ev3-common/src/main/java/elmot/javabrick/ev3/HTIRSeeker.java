package elmot.javabrick.ev3;

import elmot.javabrick.ev3.impl.SensorFactory;

import java.io.IOException;

/**
 * @author elmot
 */
public class HTIRSeeker extends SensorFactory {
    public HTIRSeeker(EV3Brick brick) {
        super(brick);
    }


    public int read(int daisyChainLevel, PORT port) throws IOException {
        return readRawByte(daisyChainLevel, port);
    }

  }
