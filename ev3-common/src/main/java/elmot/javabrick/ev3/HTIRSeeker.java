package elmot.javabrick.ev3;

import elmot.javabrick.ev3.impl.SensorFactory;

import java.io.IOException;

/**
 * @author elmot
 */
public class HTIRSeeker extends SensorFactory {
    public HTIRSeeker(EV3 brick) {
        super(brick);
    }

    public void setMode(int daisyChainLevel, PORT port, MODE mode) throws IOException {
        super.setMode(daisyChainLevel, port, mode.val);
    }

    public int read(int daisyChainLevel, PORT port) throws IOException {
        return readRawByte(daisyChainLevel, port);
    }

    public enum MODE {
        DIR_DC(0), DIR_AC(1);

        private final int val;

        private MODE(int val) {
            this.val = val;
        }
    }
  }
