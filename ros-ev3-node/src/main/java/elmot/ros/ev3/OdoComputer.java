package elmot.ros.ev3;

import geometry_msgs.Point;
import geometry_msgs.TransformStamped;
import geometry_msgs.Vector3;
import nav_msgs.Odometry;
import org.ros.message.Time;

/**
* @author elmot
*         Date: 18.08.14
*/
public class OdoComputer {

    private final double wheelRadiusCm;
    private final double wheelBaseCm;
    private double lastSample;

    public OdoComputer(double wheelRadiusCm, double wheelBaseCm, Time start) {
        this.wheelRadiusCm = wheelRadiusCm;
        this.wheelBaseCm = wheelBaseCm;
        this.lastSample = start.toSeconds();
    }

    double x = 400;
    double y = 400;
    double theta = 0;
    long oldTachoL = 0;
    long oldTachoR = 0;


    public void computeOdometry(long tachoL, long tachoR, Odometry odometry, TransformStamped transformStamped) {
        //TODO do nothing if no change
        long dL = tachoL - oldTachoL;
        oldTachoL = tachoL;
        long dR = tachoR - oldTachoR;
        oldTachoR = tachoR;
        double dTrackL = dL * Math.PI / 180 * wheelRadiusCm;
        double dTrackR = dR * Math.PI / 180 * wheelRadiusCm;
        double dTrack = dTrackR - dTrackL;
        double dTrackAvr = (dTrackR + dTrackL) / 2;
        double dTurnAngle = dTrack / wheelBaseCm ;
        double turnRadius = dTrackAvr / dTurnAngle;
        double dx;
        double dy;
        if (Double.isInfinite(turnRadius) || Double.isNaN(turnRadius)) {
            dx = dTrackAvr * Math.cos(theta);
            dy = dTrackAvr * Math.sin(theta);
        } else {
            double turnAngle = theta - Math.PI/2;
            dx = turnRadius * (Math.cos(turnAngle + dTurnAngle) - Math.cos(turnAngle));
            dy = turnRadius * (Math.sin(turnAngle + dTurnAngle) - Math.sin(turnAngle));
        }
        double newSample = odometry.getHeader().getStamp().toSeconds();
        double dTime = newSample - lastSample;
        lastSample = newSample;
        x += dx;
        y += dy;
        theta += dTurnAngle;
        if (theta < 0) theta += 2 * Math.PI;
        else if (theta > 2 * Math.PI) theta -= 2 * Math.PI;
        Point position = odometry.getPose().getPose().getPosition();
        position.setX(x);
        position.setY(y);

        geometry_msgs.Quaternion orientation = odometry.getPose().getPose().getOrientation();
        rollPitchYaw(0, 0, theta, orientation);

        Vector3 linear = odometry.getTwist().getTwist().getLinear();
        linear.setX(dx / dTime);
        linear.setY(dy / dTime);
        linear.setZ(0);
        Vector3 angular = odometry.getTwist().getTwist().getAngular();
        angular.setX(0);
        angular.setY(0);
        angular.setZ(dTurnAngle / dTime);

        Vector3 translation = transformStamped.getTransform().getTranslation();
        translation.setX(x);
        translation.setY(y);
        translation.setZ(0);
        rollPitchYaw(0, 0, theta, transformStamped.getTransform().getRotation());
    }
    private static void rollPitchYaw(double roll, double pitch, double yaw, geometry_msgs.Quaternion q) {
        double halfYaw = yaw * 0.5;
        double halfPitch = pitch * 0.5;
        double halfRoll = roll * 0.5;
        double cosYaw = Math.cos(halfYaw);
        double sinYaw = Math.sin(halfYaw);
        double cosPitch = Math.cos(halfPitch);
        double sinPitch = Math.sin(halfPitch);
        double cosRoll = Math.cos(halfRoll);
        double sinRoll = Math.sin(halfRoll);
        q.setX(sinRoll * cosPitch * cosYaw - cosRoll * sinPitch * sinYaw);//x
        q.setY(cosRoll * sinPitch * cosYaw + sinRoll * cosPitch * sinYaw); //y
        q.setZ(cosRoll * cosPitch * sinYaw - sinRoll * sinPitch * cosYaw);//z
        q.setW(cosRoll * cosPitch * cosYaw + sinRoll * sinPitch * sinYaw);
    }
}
