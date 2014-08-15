package elmot.ros.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import org.apache.commons.logging.LogFactory;

import java.util.logging.LogManager;

/**
 * @author elmot
 *         Date: 15.08.14
 */
public class RosMasterClosureActivity extends Activity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if(which == AlertDialog.BUTTON_POSITIVE)
                {
                    stopService(new Intent(RosMasterClosureActivity.this,RosMasterService.class));
                    Intent intent = new Intent(RosMasterClosureActivity.this, LegoRosActivity.class);
                    intent.setAction(LegoRosActivity.EXIT_ACTION);
                    startActivity(intent);
                }
                RosMasterClosureActivity.this.finish();
            }
        };
        builder.setTitle("ROS Master Node")
                .setMessage("Shutdown ROS master node and the whole application?")
                .setIcon(R.drawable.ic_ros_org)
                .setPositiveButton("Yes", listener)
                .setNegativeButton("Keep running",listener);
        builder.create().show();
    }

}