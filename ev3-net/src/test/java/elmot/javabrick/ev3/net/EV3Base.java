package elmot.javabrick.ev3.net;

import elmot.javabrick.ev3.EV3Brick;
import elmot.javabrick.ev3.EV3FactoryNet;

import java.net.SocketException;
import java.util.List;

/**
 * @author elmot
 */
public class EV3Base {
    public static EV3Brick openBlock() throws SocketException {
        try (EV3FactoryNet ev3FactoryNet = new EV3FactoryNet()) {
            ev3FactoryNet.open();
            ev3FactoryNet.waitForDiscovery();
            List<EV3Brick> ev3Bricks = ev3FactoryNet.listDiscovered();
            if (ev3Bricks.isEmpty()) throw new RuntimeException("No bricks in a range");
            return ev3Bricks.get(0);
        }
    }

}
