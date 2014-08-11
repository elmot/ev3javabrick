package elmot.javabrick.ev3.android.usb;

import android.hardware.usb.*;
import android.util.Log;
import elmot.javabrick.ev3.EV3Brick;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

public class EV3BrickUsbAndroid extends EV3Brick {
    public static final String LOG_TAG = "USB/EV3";
    private UsbDevice usbDevice;
    private final UsbManager usbManager;

    public static final int EV3_BLOCK_SIZE = 1024;

    public EV3BrickUsbAndroid(UsbManager usbManager) {
        this.usbManager = usbManager;
    }

    @Override
    public void ensureOpen() throws IOException {
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        for (Map.Entry<String, UsbDevice> deviceEntry : deviceList.entrySet()) {
            UsbDevice listedDevice = deviceEntry.getValue();
            if (usbDevice == null && listedDevice.getVendorId() == 1684 && listedDevice.getProductId() == 5)
                usbDevice = listedDevice;
        }
        if (usbDevice == null) throw new IOException("EV3 is not found!");
    }

    @Override
    public void close() throws Exception {
    }

    @Override
    public byte[] dataExchange(byte[] cmd) throws IOException {
        UsbInterface anInterface = usbDevice.getInterface(0);
        UsbEndpoint in = null;
        UsbEndpoint out = null;
        for (int i = 0; i < anInterface.getEndpointCount(); i++) {
            UsbEndpoint endpoint = anInterface.getEndpoint(i);
            if (endpoint.getDirection() == UsbConstants.USB_DIR_IN)
                in = endpoint;
            if (endpoint.getDirection() == UsbConstants.USB_DIR_OUT)
                out = endpoint;
        }
        if (in == null || out == null)
            throw new IOException("Endpoints are not found");
        UsbDeviceConnection conn = usbManager.openDevice(usbDevice);
        try {
            if (!conn.claimInterface(anInterface, true)) {
                throw new IOException("Can not claim EV3");
            }
            cancelPending(in, conn);
            try {
                ByteBuffer outBuffer = ByteBuffer.allocate(EV3_BLOCK_SIZE).order(ByteOrder.LITTLE_ENDIAN);
                UsbRequest outRequest = new UsbRequest();
                outBuffer.put(cmd);
                if (!outRequest.initialize(conn, out))
                    throw new IOException("Can not initialize OUT request");
                try {
                    if (!outRequest.queue(outBuffer, EV3_BLOCK_SIZE))
                        throw new IOException("Can not queue OUT request");
                    for (UsbRequest usbRequest; (usbRequest = conn.requestWait()) != outRequest; ) {
                        if (usbRequest == null)
                            throw new IOException("OUT Request wait error");
                    }
                } finally {
                    outRequest.close();
                }
                while (true) {
                    UsbRequest inRequest = new UsbRequest();
                    ByteBuffer inBuffer = ByteBuffer.allocate(EV3_BLOCK_SIZE).order(ByteOrder.LITTLE_ENDIAN);
                    if (!inRequest.initialize(conn, in))
                        throw new IOException("Can not initialize IN request");
                    try {
                        if (!inRequest.queue(inBuffer, EV3_BLOCK_SIZE))
                            throw new IOException("Can not queue IN request");
                        UsbRequest usbRequest = conn.requestWait();
                        if (usbRequest == null)
                            throw new IOException("IN Request wait error");
                        inBuffer.rewind();
                        int length = inBuffer.getShort(0) & 0xffff;
                        if (length < 3 || length > EV3_BLOCK_SIZE - 2) {
                            Log.w(LOG_TAG, "Extra response in queue");
                            continue;
                        }
                        byte[] result = new byte[length + 2];
                        inBuffer.get(result);
                        return result;
                    } finally {
                        inRequest.close();
                    }
                }
            } finally {
                conn.releaseInterface(anInterface);
            }
        } finally {
            conn.close();
        }
    }

    private void cancelPending(UsbEndpoint in, UsbDeviceConnection conn) {
        UsbRequest usbRequest = new UsbRequest();
        usbRequest.initialize(conn, in);
        try {
            boolean cancel = usbRequest.cancel();
            if (cancel) Log.i(LOG_TAG, "cancelling pending");
        } finally {
            usbRequest.close();
        }
    }
}