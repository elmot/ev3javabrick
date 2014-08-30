package elmot.javabrick.ev3.test;

import elmot.javabrick.ev3.EV3;
import elmot.javabrick.ev3.EV3FactoryUsb;
import elmot.javabrick.ev3.impl.Command;
import elmot.javabrick.ev3.impl.CommandBlock;
import elmot.javabrick.ev3.impl.Response;
import org.junit.Ignore;
import org.junit.Test;

import javax.usb.UsbException;
import java.io.IOException;
import java.util.List;

/**
 * @author elmot
 */
public class MainVoltageUsb {
    @Ignore
    @Test
    public void testSoundVolts() throws IOException, InterruptedException, UsbException {
        EV3 ev3 = findBrick();
        ev3.SYSTEM.playTone(100, 880, 700);
        for (long startMs = System.currentTimeMillis(); System.currentTimeMillis() - startMs < 10000; ) {
            float vBatt = ev3.SYSTEM.getVBatt();
            float iBatt = ev3.SYSTEM.getIBatt();
            System.out.println("v/i Batt = " + vBatt + "/" + iBatt);
        }
    }

    private EV3 findBrick() throws UsbException {
        List<EV3> ev3s = EV3FactoryUsb.listDiscovered();
        if (ev3s.isEmpty()) {
            throw new RuntimeException("No brick is found");
        }
        return ev3s.get(0);
    }

    public static final int CMD_SOUND = 0x94;
    public static final int CMD_UI_READ = 0x81;

    private static final int SUBCMD_TONE = 1;
    private static final int SUBCMD_GET_VBATT = 1;
    private static final int SUBCMD_GET_IBATT = 2;

    @Ignore
    @Test
    public void testMultiCommand() throws IOException, InterruptedException, UsbException {
        EV3 ev3 = findBrick();

        Command commandBeep = new Command(CMD_SOUND);
        commandBeep.addByte(SUBCMD_TONE);
        commandBeep.addIntConstantParam(50);
        commandBeep.addIntConstantParam(200);
        commandBeep.addIntConstantParam(300);

        Command commandVBatt = new Command(CMD_UI_READ, 4);
        commandVBatt.addByte(SUBCMD_GET_VBATT);
        commandVBatt.addShortGlobalVariable(0);
        Command commandIBatt = new Command(CMD_UI_READ, 4);
        commandIBatt.addByte(SUBCMD_GET_IBATT);
        commandIBatt.addShortGlobalVariable(0);

        CommandBlock commandBlock = new CommandBlock(commandBeep, commandVBatt, commandIBatt);
        for (int i = 0; i < 10; i++) {
            Response run = commandBlock.run(ev3, float.class, float.class);
            float iBatt = run.getFloat(0);
            float vBatt = run.getFloat(1);
            System.out.printf("Vbatt: %fV; Ibatt: %fmA\n", vBatt, iBatt);
            Thread.sleep(1000);
        }
    }


}
