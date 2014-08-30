package elmot.ros.testmaster;

import org.ros.RosCore;
import org.ros.node.DefaultNodeMainExecutor;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

/**
 * @author elmot
 *         Date: 15.08.14
 */
public class OdomNode {
    public static final String HOST = "192.168.1.41";
    private static RosCore rosCore;

    public static void main(String[] args) {
        rosCore = RosCore.newPublic(HOST, 11311);
        rosCore.start();
        NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(HOST);
        NodeMainExecutor nodeMainExecutor = DefaultNodeMainExecutor.newDefault();
        nodeMainExecutor.execute(new FakeOdo(),nodeConfiguration);
    }

}
