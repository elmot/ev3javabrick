package elmot.javabrick.ev3.android;

import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import elmot.javabrick.ev3.PORT;
import elmot.javabrick.ev3.android.usb.EV3UsbAndroid;

import java.io.IOException;


public class LegoTask extends LegoTaskBase {
    public LegoTask(Ev3Activity ev3Activity) {
        super(ev3Activity);
    }

    protected void runBrick(EV3UsbAndroid brick) throws IOException, InterruptedException {
        showProgress("Tone play");
        brick.SYSTEM.playTone(50, 330, 300);
        showProgress("Tone played");
/*

        brick.MOTOR.direction(MotorFactory.MOTORSET.A, MotorFactory.DIR.FORWARD);
        brick.MOTOR.powerTime(0, MotorFactory.MOTORSET.A, -50, 0, 1000, 0, MotorFactory.BRAKE.COAST);
        Thread.sleep(1000);
        brick.MOTOR.stop(MotorFactory.MOTORSET.A, MotorFactory.BRAKE.COAST);

        showProgress("Go!");
        brick.MOTOR.powerTime(0, MotorFactory.MOTORSET.BC, 100, 0, 1500, 0, MotorFactory.BRAKE.COAST);
        brick.MOTOR.waitForCompletion(0, MotorFactory.MOTORSET.BC);

        brick.MOTOR.powerTime(0, MotorFactory.MOTORSET.B, 50, 0, 1500, 0, MotorFactory.BRAKE.COAST);
        brick.MOTOR.powerTime(0, MotorFactory.MOTORSET.C, -50, 0, 1500, 0, MotorFactory.BRAKE.COAST);
        brick.MOTOR.waitForCompletion(0, MotorFactory.MOTORSET.BC);

        brick.SYSTEM.playTone(50, 700, 300);

        showProgress("Catch!");
        brick.MOTOR.resetTacho(0, MotorFactory.MOTORSET.A);
        brick.MOTOR.powerTime(0, MotorFactory.MOTORSET.A, 100, 0, 1000, 0, MotorFactory.BRAKE.BRAKE);
        Thread.sleep(500);

        brick.MOTOR.direction(MotorFactory.MOTORSET.A, MotorFactory.DIR.FORWARD);
        brick.MOTOR.powerStep(0, MotorFactory.MOTORSET.A, -50, 0, 700, 0, MotorFactory.BRAKE.COAST);
        Thread.sleep(1500);

        brick.MOTOR.stop(MotorFactory.MOTORSET.A, MotorFactory.BRAKE.COAST);
        brick.SYSTEM.playTone(50, 140, 1300);
*/
        for (int i = 0; i < 1000; i++) {
            float read = brick.IR.readProximity(0, PORT.P4);
            Thread.sleep(200);
            showProgress("Proximity: " + read);
            Result lastDecodedBarcode = getLastDecodedBarcode();
            if (lastDecodedBarcode != null && (System.currentTimeMillis() - lastDecodedBarcode.getTimestamp()) < 500) {
                String message = "Found barcode: " + lastDecodedBarcode.getText();
                ResultPoint[] resultPoints = lastDecodedBarcode.getResultPoints();
                if (resultPoints != null && resultPoints.length == 2) {
                    message+= "; Size: " + ResultPoint.distance(resultPoints[0], resultPoints[1]);
                }
                showProgress(message);

            }
        }
        showProgress("Finish");
    }

}
