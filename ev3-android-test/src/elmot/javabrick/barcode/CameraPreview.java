package elmot.javabrick.barcode;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.google.zxing.*;
import com.google.zxing.client.android.camera.CameraConfigurationUtils;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.oned.Code39Reader;
import elmot.javabrick.ev3.android.Constants;

import java.io.IOException;

/**
 * /** A basic Camera preview class
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private final com.google.zxing.Reader barCodeReader;


    private SurfaceHolder mHolder;
    private Camera mCamera;
    private volatile Result lastDecoded = null;

    public static Camera getCameraInstance() {
        Camera c = null;
        int cameraIndex = 0;
        for (int i = Camera.getNumberOfCameras() - 1; i >= 0; i--) {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Constants.CAMERA_FACING) {
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
            CameraConfigurationUtils.setFocus(parameters, true, false, true);
            Camera.Size biggestSize = null;

            for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
                if (biggestSize == null || biggestSize.width < size.width)
                    biggestSize = size;
            }
            if (biggestSize != null) {
                parameters.setPreviewSize(biggestSize.width, biggestSize.height);
            }

            for (Camera.Size size : parameters.getSupportedPictureSizes()) {
                if (biggestSize == null || biggestSize.width < size.width)
                    biggestSize = size;
            }
            if (biggestSize != null) {
                parameters.setPictureSize(biggestSize.width, biggestSize.height);
            }
            for (Integer pictureFormat : parameters.getSupportedPictureFormats()) {
                if (pictureFormat == ImageFormat.NV21)
                    parameters.setPictureFormat(ImageFormat.NV21);
            }
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
        barCodeReader = new Code39Reader();
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setPreviewDisplay(holder);
            startPreview();
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
            startPreview();

        } catch (Exception e) {
            Log.d(Constants.LOG_TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    private final Object lock = new Object();

    public BinaryBitmap getCameraImage() {
        final BinaryBitmap[] dataResult = {null};
        mCamera.lock();
        mCamera.takePicture(null, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        if (data == null) return;
                        Camera.Parameters parameters = camera.getParameters();
                        Camera.Size pictureSize = parameters.getPictureSize();
                        LuminanceSource luminanceSource;
                        luminanceSource = new PlanarYUVLuminanceSource(data, pictureSize.width, pictureSize.height, 0, 0, pictureSize.width, pictureSize.height, false);
                        dataResult[0] = new BinaryBitmap(new HybridBinarizer(luminanceSource));
                    }
                }, null,
                new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        synchronized (lock) {
                            if (dataResult[0] == null) {
                                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                                int width = bitmap.getWidth();
                                int height = bitmap.getHeight();
                                int[] imageARGB = new int[width * height];
                                bitmap.getPixels(imageARGB, 0, width, 0, 0, width, height);
                                LuminanceSource luminanceSource = new RGBLuminanceSource(width, height, imageARGB);
                                dataResult[0] = new BinaryBitmap(new HybridBinarizer(luminanceSource));
                            }
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
                startPreview();
                return null;
            }
        }
        startPreview();
        return dataResult[0];
    }

    private void startPreview() {
        mCamera.startPreview();
        mCamera.setPreviewCallback(
                new Camera.PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] data, Camera camera) {
                        Camera.Size previewSize = camera.getParameters().getPreviewSize();
                        LuminanceSource luminanceSource = new PlanarYUVLuminanceSource(data, previewSize.width, previewSize.height, 0, 0, previewSize.width, previewSize.height, false);
                        Log.i(Constants.LOG_TAG, "Camera data: " + camera.getParameters().flatten());
                        try {
                            Result decode = barCodeReader.decode(new BinaryBitmap(new HybridBinarizer(luminanceSource)), Constants.BARCODE_HINTS);
                            lastDecoded = decode;
                            Log.d(Constants.LOG_TAG, "Barcode: " + decode);
                        } catch (Exception ignored) {
                        }
                    }
                }
        );
    }

    /**
     * @return read barcode value or null
     */

    public synchronized Result scanPreciseBarcode() {
        try {
            Result decode = barCodeReader.decode(getCameraImage());
            String text = decode.getText();
            String msg = "Barcode: " + text;
            ResultPoint[] resultPoints = decode.getResultPoints();
            if (resultPoints != null && resultPoints.length == 2) {
                msg += "; bounds: " + resultPoints[0].toString() + resultPoints[1].toString();
            }
            Log.d(Constants.LOG_TAG, msg);
            return decode;
        } catch (Exception ignored) {
            Log.d(Constants.LOG_TAG, "Barcode is not found");
            return null;
        }
    }

    public Result getLastDecoded() {
        return lastDecoded;
    }
}