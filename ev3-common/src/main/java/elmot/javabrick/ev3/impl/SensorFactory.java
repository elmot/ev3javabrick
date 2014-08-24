package elmot.javabrick.ev3.impl;

import elmot.javabrick.ev3.EV3;
import elmot.javabrick.ev3.PORT;

import java.io.IOException;

/**
 * @author elmot
 */
public class SensorFactory extends FactoryBase {

    public static final int CMD_INPUT_DEVICE = 0x99;
    public static final int CMD_INPUT_READ = 0x9a;
    public static final int CMD_INPUT_READ_SI = 0x9d;
    public static final int CMD_INPUT_READ_EXT = 0x9d;
    public static final int SUBCMD_GET_RAW = 11;

    private static final int SUBCMD_STOP_ALL = 13;
    public static final int SUBCMD_CLR_CHANGES = 26;
    public static final int SUBCMD_READ_SI = 29;
    public static final int SUBCMD_GET_BUMPS = 31;


    protected SensorFactory(EV3 ev3) {
        super(ev3);
    }
    protected float readSI(int daisyChainLevel, PORT port, int mode) throws IOException {
        Command command = new Command(CMD_INPUT_READ_SI, 4);
        command.addByte(daisyChainLevel);
        command.addByte(port.portNum);
        command.addIntConstantParam(0);
        command.addByte(mode);
        command.addShortGlobalVariable(0);
        Response response = run(command, float.class);
        return response.getFloat(0);
    }

    protected void setMode(int daisyChainLevel, PORT port, int mode) throws IOException {
        Command command = new Command(0x9d, 4);
        command.addByte(daisyChainLevel);
        command.addByte(port.portNum);
        command.addIntConstantParam(0);
        command.addByte(mode);
        command.addShortGlobalVariable(0);
        run(command);
    }

    public void stopAll(int daisyLevel) throws IOException {
        Command command = new Command(CMD_INPUT_DEVICE, 0);
        command.addByte(SUBCMD_STOP_ALL);
        command.addByte(daisyLevel);
        run(command);
    }

    protected int readRaw(int daisyLevel, PORT port) throws IOException {
        Command command = new Command(CMD_INPUT_DEVICE, 4);
        command.addByte(SUBCMD_GET_RAW);
        command.addByte(daisyLevel);
        command.addByte(port.portNum);
        command.addShortGlobalVariable(0);
        Response response = run(command, int.class);
        return response.getInt(0);
    }

    protected int readRawByte(int daisyLevel, PORT port) throws IOException {
        Command command = new Command(CMD_INPUT_DEVICE, 4);
        command.addByte(SUBCMD_GET_RAW);
        command.addByte(daisyLevel);
        command.addByte(port.portNum);
        command.addShortGlobalVariable(0);
        Response response = run(command, byte.class);
        return response.getByte(0);
    }

    public void clearChanges(int daisyLevel, PORT port) throws IOException {
        Command command = new Command(CMD_INPUT_DEVICE, 4);
        command.addByte(SUBCMD_CLR_CHANGES);
        command.addByte(daisyLevel);
        command.addByte(port.portNum);
        run(command);
    }

    protected int getRead(int daisyChainLevel, PORT port, byte mode) throws IOException {
        Command command = new Command(CMD_INPUT_READ, 4);
        command.addByte(daisyChainLevel);
        command.addByte(port.portNum);
        command.addIntConstantParam(0);
        command.addByte(mode);
        command.addShortGlobalVariable(0);
        Response run = run(command, byte.class);
        return run.getInt(0);
    }

}
