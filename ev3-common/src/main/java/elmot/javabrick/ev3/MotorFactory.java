package elmot.javabrick.ev3;

import elmot.javabrick.ev3.impl.Command;
import elmot.javabrick.ev3.impl.FactoryBase;
import elmot.javabrick.ev3.impl.Response;

import java.io.IOException;

/**
 * @author elmot
 */
public class MotorFactory extends FactoryBase
{

    public static final int CMD_DIR = 0xA7;
    public static final int CMD_TEST = 0xA9;
    public static final int CMD_STEP_POWER = 0xAC;
    private static final int CMD_TIME_POWER = 0xAD;
    private static final int CMD_STEP_SPEED = 0xAE;
    private static final int CMD_TIME_SPEED = 0xAF;
    private static final int CMD_STEP_SYNC = 0xB0;
    private static final int CMD_TIME_SYNC = 0xB1;

    protected MotorFactory(EV3 brick)
    {
        super(brick);
    }

    public void start(MOTORSET motors) throws IOException
    {
        start(0, motors);
    }

    public void start(int daisyChainLevel, MOTORSET motors) throws IOException
    {
        Command command = new Command(0xA6);
        command.addByte(daisyChainLevel);
        command.addByte(motors.val);
        run(command);
    }

    public void speed(MOTORSET motors, int speed) throws IOException
    {
        speed(0, motors, speed);
    }

    public void speed(int daisyChainLevel, MOTORSET motors, int speed) throws IOException
    {
        Command command = new Command(0xA5);
        command.addByte(daisyChainLevel);
        command.addByte(motors.val);
        command.addLongOneByte(speed);
        run(command);
    }

    public void power(MOTORSET motors, int power) throws IOException
    {
        power(0, motors, power);
    }

    public void power(int daisyChainLevel, MOTORSET motors, int power) throws IOException
    {
        Command command = new Command(0xA4);
        command.addByte(daisyChainLevel);
        command.addByte(motors.val);
        command.addLongOneByte(power);
        run(command);
    }

    public long getTacho(MOTOR motor) throws IOException
    {
        return getTacho(0, motor);
    }

    public long getTacho(int daisyChainLevel, MOTOR motor) throws IOException
    {
        Command command = new Command(0xB3, 4);
        command.addByte(daisyChainLevel);
        command.addByte(motor.val);
        command.addShortGlobalVariable(0);
        Response response = run(command, int.class);
        return response.getInt(0);
    }

    public void stop(int daisyChainLevel, MOTORSET motors, BRAKE brake) throws IOException
    {
        Command command = new Command(0xA3);
        command.addByte(daisyChainLevel);
        command.addByte(motors.val);
        command.addByte(brake.val);
        run(command);
    }

    public void stop(MOTORSET motors, BRAKE brake) throws IOException
    {
        stop(0, motors, brake);
    }

    public void waitForCompletion(int daisyChainLevel, MOTORSET motors) throws IOException
    {
        Command command = new Command(0xAA);
        command.addByte(daisyChainLevel);
        command.addByte(motors.val);
        run(command);
    }

    public void waitForCompletion(MOTORSET motors) throws IOException
    {
        waitForCompletion(0, motors);
    }

    public void resetTacho(int daisyChainLevel, MOTORSET motors) throws IOException
    {
        Command command = new Command(0xB2);
        command.addByte(daisyChainLevel);
        command.addByte(motors.val);
        run(command);
    }

    public void resetTacho(MOTORSET motors) throws IOException
    {
        resetTacho(0, motors);
    }

    public void direction(MOTORSET motors, DIR direction) throws IOException
    {
        direction(0, motors, direction);
    }

    public void direction(int daisyChainLevel, MOTORSET motors, DIR direction) throws IOException
    {
        Command command = new Command(CMD_DIR);
        command.addByte(daisyChainLevel);
        command.addByte(motors.val);
        command.addLongOneByte(direction.val);
        run(command);
    }

    /**
     * Run immediately
     *
     * @param daisyChainLevel 0 for single brick
     * @param motors          apply to motors
     * @param power           -100...+100
     * @param stepRampUp      Steps used to ramp up
     * @param stepMove        Steps used for constant speed
     * @param stepRampDown    Steps used to ramp down
     * @param brake           What to do after
     */
    public void powerStep(int daisyChainLevel, MOTORSET motors, int power, int stepRampUp, int stepMove, int stepRampDown, BRAKE brake) throws IOException
    {
        Command command = new Command(CMD_STEP_POWER);
        command.addByte(daisyChainLevel);
        command.addByte(motors.val);
        command.addIntConstantParam(power);
        command.addIntConstantParam(stepRampUp);
        command.addIntConstantParam(stepMove);
        command.addIntConstantParam(stepRampDown);
        command.addByte(brake.val);
        run(command);
    }

    /**
     * Run immediately
     *
     * @param daisyChainLevel 0 for single brick
     * @param motors          apply to motors
     * @param power           -100...+100
     * @param timeRampUp      ramp up milliseconds
     * @param timeMove        constant speed milliseconds
     * @param timeRampDown    ramp down milliseconds
     * @param brake           What to do after
     */
    public void powerTime(int daisyChainLevel, MOTORSET motors, int power, int timeRampUp, int timeMove, int timeRampDown, BRAKE brake) throws IOException
    {
        Command command = new Command(CMD_TIME_POWER);
        command.addByte(daisyChainLevel);
        command.addByte(motors.val);
        command.addIntConstantParam(power);
        command.addIntConstantParam(timeRampUp);
        command.addIntConstantParam(timeMove);
        command.addIntConstantParam(timeRampDown);
        command.addByte(brake.val);
        run(command);
    }

    /**
     * Run immediately
     *
     * @param daisyChainLevel 0 for single brick
     * @param motors          apply to motors
     * @param speed           -100...+100
     * @param stepRampUp      Steps used to ramp up
     * @param stepMove        Steps used for constant speed
     * @param stepRampDown    Steps used to ramp down
     * @param brake           What to do after
     */
    public void speedStep(int daisyChainLevel, MOTORSET motors, int speed, int stepRampUp, int stepMove, int stepRampDown, BRAKE brake) throws IOException
    {
        Command command = new Command(CMD_STEP_SPEED);
        command.addByte(daisyChainLevel);
        command.addByte(motors.val);
        command.addIntConstantParam(speed);
        command.addIntConstantParam(stepRampUp);
        command.addIntConstantParam(stepMove);
        command.addIntConstantParam(stepRampDown);
        command.addByte(brake.val);
        run(command);
    }

    /**
     * Run immediately
     *
     * @param daisyChainLevel 0 for single brick
     * @param motors          apply to motors
     * @param speed           -100...+100
     * @param timeRampUp      ramp up milliseconds
     * @param timeMove        constant speed milliseconds
     * @param timeRampDown    ramp down milliseconds
     * @param brake           What to do after
     */
    public void speedTime(int daisyChainLevel, MOTORSET motors, int speed, int timeRampUp, int timeMove, int timeRampDown, BRAKE brake) throws IOException
    {
        Command command = new Command(CMD_TIME_SPEED);
        command.addByte(daisyChainLevel);
        command.addByte(motors.val);
        command.addIntConstantParam(speed);
        command.addIntConstantParam(timeRampUp);
        command.addIntConstantParam(timeMove);
        command.addIntConstantParam(timeRampDown);
        command.addByte(brake.val);
        run(command);
    }

    /**
     * Move with turn, start immediately
     *
     * @param daisyChainLevel 0 for single brick
     * @param motors          apply to motors
     * @param speed           -100...+100
     * @param turn            -200..+200 Turn ratio between two syncronized motors
     * @param steps           steps to go
     * @param brake           What to do after
     * @throws java.io.IOException
     */
    public void stepSync(int daisyChainLevel, MOTORSET motors, int speed, int turn, int steps, BRAKE brake) throws IOException
    {
        Command command = new Command(CMD_STEP_SYNC);
        command.addByte(daisyChainLevel);
        command.addByte(motors.val);
        command.addIntConstantParam(speed);
        command.addIntConstantParam(turn);
        command.addIntConstantParam(steps);
        command.addIntConstantParam(brake.val);
        run(command);
    }

    /**
     * Move with turn, start immediately
     *
     * @param daisyChainLevel 0 for single brick
     * @param motors          apply to motors
     * @param speed           -100...+100
     * @param turn            -200..+200 Turn ratio between two syncronized motors
     * @param time            milliseconds to go
     * @param brake           What to do after
     * @throws java.io.IOException
     */
    public void timeSync(int daisyChainLevel, MOTORSET motors, int speed, int turn, int time, BRAKE brake) throws IOException
    {
        Command command = new Command(CMD_TIME_SYNC);
        command.addByte(daisyChainLevel);
        command.addByte(motors.val);
        command.addIntConstantParam(speed);
        command.addIntConstantParam(turn);
        command.addIntConstantParam(time);
        command.addIntConstantParam(brake.val);
        run(command);
    }

    public enum MOTOR
    {
        A(0), B(1), C(2), D(3);
        public final byte val;

        private MOTOR(int val)
        {
            this.val = (byte) val;
        }
    }

    public enum BRAKE
    {
        COAST(0), BRAKE(1);
        private byte val;

        private BRAKE(int val)
        {
            this.val = (byte) val;
        }
    }

    public enum DIR
    {
        FORWARD(1), BACK(-11), TOGGLE(0);
        private byte val;

        private DIR(int val)
        {
            this.val = (byte) val;
        }
    }

    public enum MOTORSET
    {
        A(1), B(2), C(4), D(8),
        AB(3), AC(5), AD(9), BC(6), BD(10), CD(12),
        ABC(0x7), ABD(11), ACD(13),
        BCD(14), ABCD(15);
        public final byte val;

        private MOTORSET(int val)
        {
            this.val = (byte) val;
        }
    }

    public static MOTORSET motorset(byte val)
    {
        switch (val)
        {
            case  1: return MOTORSET.A;
            case  2: return MOTORSET.B;
            case  3: return MOTORSET.AB;
            case  4: return MOTORSET.C;
            case  5: return MOTORSET.AC;
            case  6: return MOTORSET.BC;
            case  7: return MOTORSET.ABC;
            case  8: return MOTORSET.D;
            case  9: return MOTORSET.AD;
            case 10: return MOTORSET.BD;
            case 11: return MOTORSET.ABD;
            case 12: return MOTORSET.CD;
            case 13: return MOTORSET.ACD;
            case 14: return MOTORSET.BCD;
            case 15: return MOTORSET.ABCD;
        }
        throw new IllegalArgumentException(Byte.toString(val));
    }

    public static MotorFactory.MOTORSET motorset(MotorFactory.MOTOR motor1, MotorFactory.MOTOR motor2)
    {
        return motorset((byte)((1 << motor1.val) +  (1 << motor2.val)));
    }

}
