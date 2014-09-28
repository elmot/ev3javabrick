package elmot.javabrick.nxt;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public abstract class NXT implements AutoCloseable {

    protected abstract void ensureOpen() throws NXTException;

    @Override
    public abstract void close();

    protected abstract ByteBuffer dataExchange(ByteBuffer cmd, int expectedResponseSize) throws NXTException;

    private void checkResponse(ByteBuffer buf, int expectedCmd) throws NXTException {
        if (buf.get(0) != 2) throw new NXTException(String.format("Return flag %d instead of 2", buf.get(0)));
        if (buf.get(1) != expectedCmd)
            throw new NXTException(String.format("Returned cmd %d instead of %d", buf.get(1), expectedCmd));
        byte statusByte = buf.get(2);
        if (statusByte != 0)
            throw new NXTException(String.format("Status 0x%2x instead of 0x0", statusByte), statusByte);
    }

    private void putString(ByteBuffer cmd, String str, int maxLength) {
        for (int i = 0; i < str.length() && i <= maxLength; i++)
            cmd.put((byte) str.charAt(i));
        cmd.put((byte) 0).rewind();
    }

    private ByteBuffer newBuffer(int size, int byte0, int byte1) {
        return ByteBuffer.allocate(size).order(ByteOrder.LITTLE_ENDIAN).put(0, (byte) byte0).put(1, (byte) byte1);
    }

    private ByteBuffer run(ByteBuffer cmd, int expectedCmd, int responseSize) throws NXTException {
        ensureOpen();
        try {
            cmd.rewind();
            ByteBuffer reply = dataExchange(cmd, responseSize);
            checkResponse(reply, expectedCmd);
            return reply;
        } finally {
            close();
        }
    }


    enum VALUE_TYPE {IS_VALID, IS_CALIBRATED, SENS_TYPE, SENS_MODE, RAW, NORMALIZED, SCALED, CALIBRATED}


    @SuppressWarnings("UnusedDeclaration")
    public final SystemFactory SYSTEM = new SystemFactory();

    @SuppressWarnings("UnusedDeclaration")
    public class SystemFactory {
        private SystemFactory() {
        }

        /**
         * @param remoteInbox 0..19
         * @param localInbox  0..9
         * @param remove      if need to remove
         * @return message
         * @see "LEGO MINDSTORMS NXT Direct Commands.pdf"
         */
        public String messageRead(byte remoteInbox, byte localInbox, boolean remove) throws NXTException {
            ByteBuffer cmd = newBuffer(5, 0, 0x13).put(2, remoteInbox)
                    .put(3, localInbox).put(4, (byte) (remove ? 1 : 0));
            try {
                ByteBuffer reply = run(cmd, 0x13, 64);
                int len = reply.get(4);
                StringBuilder result = new StringBuilder(len - 1);
                for (int i = 0; i < len - 1; i++) {
                    result.append((char) reply.get(i + 5));
                }
                return result.toString();
            } catch (NXTException e) {
                if (e.getStatusByte() == 0x40) {
                    return null;
                }
                throw e;
            }
        }

        /**
         * @param inbox 0..9
         * @param msg   Ascii, 59 chars at maximum
         * @return status code, 0 if OK
         */
        public int messageWrite(byte inbox, String msg) throws NXTException {
            int len = Math.min(59, msg.length());
            ByteBuffer cmd = newBuffer(len + 5, 0, 0x09);
            cmd.put(2, inbox);
            cmd.put(3, (byte) (len + 1));
            cmd.position(4);
            putString(cmd, msg, 59);
            return run(cmd, 0x09, 3).get(2);
        }

        public void playTone(int freq, int durationMs) throws NXTException {
            ByteBuffer cmd = newBuffer(6, 0, 0x03).putShort(2, (short) freq)
                    .putShort(4, (short) durationMs);
            run(cmd, 0x3, 3);
        }

        public void stopSoundPlayback() throws NXTException {
            ByteBuffer cmd = newBuffer(2, 0, 0x0C);
            run(cmd, 0x0C, 3);
        }

        public byte startProgram(String programName) throws NXTException {
            ByteBuffer cmd = newBuffer(22, 0, 0x0);
            cmd.position(2);
            putString(cmd, programName, 19);
            return run(cmd, 0x0, 3).get(2);
        }

        /**
         * @return Volts
         */
        public double getVBatt() throws NXTException {
            ByteBuffer cmd = newBuffer(2, 0, 0x0B);
            return (run(cmd, 0x0B, 5).getShort(3) & 0xFFFF) / 1000.0d;
        }

        /**
         * @return current sleep time, ms
         */
        public long keepAlive() throws NXTException {
            ByteBuffer cmd = newBuffer(3, 0, 0x0D);
            return run(cmd, 0x0D, 7).getLong(3);
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public final Sensor SENSOR = new Sensor();

    @SuppressWarnings("UnusedDeclaration")
    public class Sensor {
        public static final byte P1 = 0;
        public static final byte P2 = 1;
        public static final byte P3 = 2;
        public static final byte P4 = 3;

        public static final byte TYPE_NO_SENSOR = 0x0;
        public static final byte TYPE_SWITCH = 0x1;
        public static final byte TYPE_TEMPERATURE = 0x2;
        public static final byte TYPE_REFLECTION = 0x3;
        public static final byte TYPE_ANGLE = 0x4;
        public static final byte TYPE_LIGHT_ACTIVE = 0x5;
        public static final byte TYPE_LIGHT_INACTIVE = 0x6;
        public static final byte TYPE_SOUND_DB = 0x7;
        public static final byte TYPE_SOUND_DBA = 0x8;
        public static final byte TYPE_CUSTOM = 0x9;
        public static final byte TYPE_LOW_SPEED = 0xA;
        public static final byte TYPE_LOW_SPEED_9V = 0xB;
        public static final byte TYPE_NO_OF_SENS_TYPES = 0xC;

        public static final byte MODE_RAW = 0x00;
        public static final byte MODE_BOOL = 0x20;
        public static final byte MODE_TRANSITION_CNT = 0x40;
        public static final byte MODE_PERIOD_COUNTER = 0x60;
        public static final byte MODE_PCT_FULL_SCALE = (byte) 0x80;
        public static final byte MODE_CELSIUS = (byte) 0xA0;
        public static final byte MODE_FAHRENHEIT = (byte) 0xC0;
        public static final byte MODE_ANGLE_STEP = (byte) 0xE0;
        public static final byte MODE_SLOP_EMASK = 0x1F;
        public static final byte MODE_MODE_MASK = (byte) 0xE0;

        private Sensor() {
        }

        void resetInputScaledValue(byte port) throws NXTException {
            ByteBuffer cmd = newBuffer(3, 0, 0x08).put(2, port);
            run(cmd, 0x08, 3);
        }

        int getInputValue(byte port, VALUE_TYPE valueType) throws NXTException {
            ByteBuffer cmd = newBuffer(3, 0, 0x07).put(2, port);
            ByteBuffer reply = run(cmd, 0x07, 16);
            switch (valueType) {
                case IS_VALID:
                    return reply.get(4);
                case IS_CALIBRATED:
                    return reply.get(5);
                case SENS_TYPE:
                    return reply.get(6);
                case SENS_MODE:
                    return reply.get(7);
                case RAW:
                    return reply.getShort(8) & 0xFFFF;
                case NORMALIZED:
                    return reply.getShort(10) & 0xFFFF;
                case SCALED:
                    return reply.getShort(12);
                case CALIBRATED:
                    return reply.getShort(14);
            }
            throw new IllegalArgumentException(" valueType");
        }

        void setInputMode(byte port, byte sensorType, byte sensorMode) throws NXTException {
            ByteBuffer cmd = newBuffer(5, 0, 0x05).put(2, port).put(3, sensorType).put(4, sensorMode);
            run(cmd, 0x05, 3);
        }

        public float readUltrasonic(byte port) {
            return 0;  //Todo
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public final Motor MOTOR = new Motor();

    @SuppressWarnings("UnusedDeclaration")
    public class Motor {

        public static final byte A = 0;
        public static final byte B = 1;
        public static final byte C = 2;

        public static final byte MODE_OFF = 0;
        public static final byte MODE_ON = 1;
        public static final byte MODE_BRAKE = 2;
        public static final byte MODE_REGULATED = 4;

        public static final byte REG_MODE_IDLE = 0;
        public static final byte REG_MODE_SPEED = 1;
        public static final byte REG_MODE_SYNC = 2;

        public static final byte STATE_IDLE = 0;
        public static final byte STATE_RAMPUP = 0x10;
        public static final byte STATE_RUNNING = 0x20;
        public static final byte STATE_RAMPDOWN = 0x40;


        private Motor() {
        }

        public long getAbsoluteTacho(byte motor) throws NXTException {
            ByteBuffer cmd = newBuffer(3, 0, 0x06).put(2, motor);
            return run(cmd, 0x06, 26).getInt(21);

        }

        /**
         * @param motor          0..2
         * @param powerSetPoint  -100..100
         * @param mode           bit field,  see Motor.MODE_XXX
         * @param regulationMode see Motor.REG_MODE_XXX
         * @param turnRatio      -100..100
         * @param runState       see Motor.STATE_XXX
         * @param tachoLimit     when to stop
         */
        public void setOutputState(byte motor, int powerSetPoint, byte mode, byte regulationMode,
                                   int turnRatio, byte runState, long tachoLimit) throws NXTException {
            ByteBuffer cmd = newBuffer(13, 0, 0x04);
            cmd.put(2, motor);
            cmd.put(3, (byte) powerSetPoint);
            cmd.put(4, mode);
            cmd.put(5, regulationMode);
            cmd.put(6, (byte) turnRatio);
            cmd.put(7, runState);
            cmd.putInt(8, (int) tachoLimit);
            run(cmd, 0x04, 3);
        }

        public void resetMotorPosition(byte motorIndex, boolean relative) throws NXTException {
            ByteBuffer cmd = newBuffer(4, 0, 0x0A).put(2, motorIndex).put(3, (byte) (relative ? 1 : 0));
            run(cmd, 0x0A, 3);
        }

        public void stopMotors() throws NXTException {
            setOutputState((byte) 0xff, 0, MODE_OFF, REG_MODE_IDLE, 0, STATE_IDLE, 0);
        }

        public void steerMotors(byte motor1, byte motor2, int powerSetPoint, int turn, long tachoLimit) throws NXTException {
            setOutputState(motor1, powerSetPoint + turn, (byte) (MODE_ON | MODE_REGULATED), REG_MODE_SPEED, turn, STATE_RUNNING, tachoLimit);
            setOutputState(motor2, powerSetPoint - turn, (byte) (MODE_ON | MODE_REGULATED), REG_MODE_SPEED, turn, STATE_RUNNING, tachoLimit);
        }
    }
}