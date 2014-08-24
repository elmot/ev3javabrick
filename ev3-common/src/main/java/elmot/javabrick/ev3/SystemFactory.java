package elmot.javabrick.ev3;

import elmot.javabrick.ev3.impl.Command;
import elmot.javabrick.ev3.impl.FactoryBase;
import elmot.javabrick.ev3.impl.Response;

import java.io.IOException;

/**
 * @author elmot
 */
public class SystemFactory extends FactoryBase{

    public static final int CMD_SOUND = 0x94;
    public static final int CMD_UI_READ = 0x81;

    private static final int SUBCMD_TONE = 1;
    private static final int SUBCMD_GET_VBATT = 1;
    private static final int SUBCMD_GET_IBATT = 2;

    SystemFactory(EV3 ev3) {
        super(ev3);
    }

    public void playTone(int volume, int frequency, int durationMs) throws IOException {
        Command command = new Command(CMD_SOUND);
        command.addByte(SUBCMD_TONE);
        command.addIntConstantParam(volume);
        command.addIntConstantParam(frequency);
        command.addIntConstantParam(durationMs);
        run(command);
    }

    public float getVBatt() throws IOException {
        Command command = new Command(CMD_UI_READ,4);
        command.addByte(SUBCMD_GET_VBATT);
        command.addShortGlobalVariable(0);
        Response response = run(command, float.class);
        return response.getFloat(0);
    }

    public float getIBatt() throws IOException {
        Command command = new Command(CMD_UI_READ,4);
        command.addByte(SUBCMD_GET_IBATT);
        command.addShortGlobalVariable(0);
        Response response = run(command, float.class);
        return response.getFloat(0);
    }
}
