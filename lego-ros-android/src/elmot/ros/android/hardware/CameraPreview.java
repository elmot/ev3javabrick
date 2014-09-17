package elmot.ros.android.hardware;

import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import elmot.ros.android.Settings;

import java.io.IOException;

//import com.google.zxing.common.HybridBinarizer;

/**
 * /** A basic Camera preview class
 */
public class CameraPreview implements SurfaceHolder.Callback {
//    private final com.google.zxing.Reader barCodeReader;
//    private final Map<DecodeHintType, Object> barcodeHints;

    //    private SurfaceHolder surfaceHolder;
    private Camera mCamera;
    private volatile boolean cameraAvailable = false;
    public static Camera getCameraInstance(int cameraFacing) {
        Camera c = null;
        int cameraIndex = 0;
        for (int i = Camera.getNumberOfCameras() - 1; i >= 0; i--) {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == cameraFacing) {
                cameraIndex = i;
                break;
            }
        }
        try {
            c = Camera.open(cameraIndex);
            Camera.Parameters parameters = c.getParameters();
/*
            CameraConfigurationUtils.setBarcodeSceneMode(parameters);
            CameraConfigurationUtils.setBestExposure(parameters, false);
            CameraConfigurationUtils.setMetering(parameters);
            CameraConfigurationUtils.setVideoStabilization(parameters);
*/
            c.setParameters(parameters);
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    public static CameraPreview init(SurfaceView surfaceView,int cameraFacing) {
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        CameraPreview cameraPreview = new CameraPreview(cameraFacing);
        surfaceHolder.addCallback(cameraPreview);
        return cameraPreview;
    }

    private CameraPreview(int cameraFacing) {
        this.mCamera = getCameraInstance(cameraFacing);
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
//        barCodeReader = new MultiFormatReader();
//        barcodeHints = new HashMap<DecodeHintType, Object>();
//        barcodeHints.put(DecodeHintType.POSSIBLE_FORMATS, Arrays.asList(BarcodeFormat.QR_CODE, BarcodeFormat.CODE_39));
//        barcodeHints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
            cameraAvailable = true;
        } catch (IOException e) {
            Log.d(Settings.LOG_TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        try {
            cameraAvailable = false;
            mCamera.stopPreview();
        } catch (RuntimeException ignored) {
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (holder.getSurface() == null) {
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            cameraAvailable = false;
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }
        //Settings to be changed
        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
            cameraAvailable = true;

        } catch (Exception e) {
            Log.d(Settings.LOG_TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    final Object lock = new Object();
    public synchronized byte[] getCameraImage() {
        if(!cameraAvailable) return null;
        final byte[][] dataResult = {null};
        synchronized (lock) {
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
            try {
                while (dataResult[0] == null && cameraAvailable)
                    lock.wait(100);
            } catch (InterruptedException e) {
                mCamera.startPreview();
                return null;
            }
        }
        mCamera.startPreview();
        return dataResult[0];
/*
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] imageARGB = new int[width * height];
        bitmap.getPixels(imageARGB, 0, width, 0, 0, width, height);
        LuminanceSource luminanceSource = new RGBLuminanceSource(width, height, imageARGB);
        return new BinaryBitmap(new HybridBinarizer(luminanceSource));
*/
    }

    public void release() {
        cameraAvailable = false;
        mCamera.release();
    }

    /**
     * @return read barcode value or null
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
     */

}