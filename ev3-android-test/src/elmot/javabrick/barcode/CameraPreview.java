package elmot.javabrick.barcode;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.google.zxing.*;
import com.google.zxing.client.android.camera.CameraConfigurationUtils;
import com.google.zxing.common.HybridBinarizer;
import elmot.javabrick.ev3.android.Constants;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * /** A basic Camera preview class
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private final com.google.zxing.Reader barCodeReader;
    private final Map<DecodeHintType, Object> barcodeHints;
    private SurfaceHolder mHolder;
    private Camera mCamera;

    public static Camera getCameraInstance() {
        Camera c = null;
        int cameraIndex = 0;
        for (int i = Camera.getNumberOfCameras() - 1; i >= 0; i--) {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                cameraIndex = i;
                break;
            }
        }
        try {
            c = Camera.open(cameraIndex);
            Camera.Parameters parameters = c.getParameters();
            CameraConfigurationUtils.setBarcodeSceneMode(parameters);
            CameraConfigurationUtils.setBestExposure(parameters, false);
            CameraConfigurationUtils.setMetering(parameters);
            CameraConfigurationUtils.setVideoStabilization(parameters);
            c.setParameters(parameters);
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    public CameraPreview(Context context) {
        super(context);
        mCamera = getCameraInstance();
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        barCodeReader = new MultiFormatReader();
        barcodeHints = new HashMap<DecodeHintType, Object>();
//        barcodeHints.put(DecodeHintType.POSSIBLE_FORMATS, Arrays.asList(BarcodeFormat.QR_CODE, BarcodeFormat.EAN_13, BarcodeFormat.CODE_39));
        barcodeHints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d(Constants.LOG_TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        mCamera.stopPreview();
        mCamera.release();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }
        //Settings to be changed
        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

        } catch (Exception e) {
            Log.d(Constants.LOG_TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    public BinaryBitmap getCameraImage() {
        final Object lock = new Object();
        final byte[][] dataResult = {null};
        mCamera.lock();
        mCamera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                synchronized (lock) {
                    dataResult[0] = data;
                    lock.notify();
                }
            }
        }
        );

        synchronized (lock) {
            try {
                while (dataResult[0] == null)
                    lock.wait();
            } catch (InterruptedException e) {
                mCamera.startPreview();
                return null;
            }
        }
        mCamera.startPreview();
        Bitmap bitmap = BitmapFactory.decodeByteArray(dataResult[0], 0, dataResult[0].length);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] imageARGB = new int[width * height];
        bitmap.getPixels(imageARGB, 0, width, 0, 0, width, height);
        LuminanceSource luminanceSource = new RGBLuminanceSource(width, height, imageARGB);
        return new BinaryBitmap(new HybridBinarizer(luminanceSource));
    }

    /**
     * @return read barcode value or null
     */
    public synchronized String scanBarcode() {
        try {
            Result decode = barCodeReader.decode(getCameraImage(), barcodeHints);
            String text = decode.getText();
            Log.i(Constants.LOG_TAG, "Barcode: " + text);
            return text;
        } catch (NotFoundException e) {
            Log.d(Constants.LOG_TAG, "Barcode is not found");
            return null;
        } catch (ChecksumException e) {
            Log.i(Constants.LOG_TAG, "Barcode checksum error");
            return null;
        } catch (FormatException e) {
            Log.i(Constants.LOG_TAG, "Barcode format error");
            return null;
        }
    }
}