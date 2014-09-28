package elmot.javabrick.nxt.android;

import android.hardware.usb.*;
import android.util.Log;
import elmot.javabrick.nxt.NXT;
import elmot.javabrick.nxt.NXTException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;

public class NXTUsbAndroid extends NXT {
    public static final String LOG_TAG = "USB/NXT";
    private UsbDevice usbDevice;
    private final UsbManager usbManager;

    public NXTUsbAndroid(UsbManager usbManager) {
        this.usbManager = usbManager;
    }

    @Override
    public void ensureOpen() throws NXTException {
        usbDevice = findDevice(usbManager);
        if (usbDevice == null) throw new NXTException("NXT is not found!");
    }

    public static UsbDevice  findDevice(UsbManager usbManager) {
        for (Map.Entry<String, UsbDevice> deviceEntry : usbManager.getDeviceList().entrySet()) {
            UsbDevice listedDevice = deviceEntry.getValue();
            if (listedDevice.getVendorId() == 1684 && listedDevice.getProductId() == 2)
                return listedDevice;
        }
        return null;
    }

    @Override
    public void close() {
    }

    @Override
    public ByteBuffer dataExchange(ByteBuffer cmd, int expectedResponseSize) throws NXTException {
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
            throw new NXTException("Endpoints are not found");
        UsbDeviceConnection conn = usbManager.openDevice(usbDevice);
        try {
            if (!conn.claimInterface(anInterface, true)) {
                throw new NXTException("Can not claim NXT");
            }
            cancelPending(in, conn);
            try {
                UsbRequest outRequest = new UsbRequest();
                if (!outRequest.initialize(conn, out))
                    throw new NXTException("Can not initialize OUT request");
                try {
                    if (!outRequest.queue(cmd, cmd.capacity()))
                    {
                        outRequest.cancel();
                        throw new NXTException("Can not queue OUT request");
                    }
                    for (UsbRequest usbRequest; (usbRequest = conn.requestWait()) != outRequest; ) {
                        if (usbRequest == null)
                            throw new NXTException("OUT Request wait error");
                    }
                } finally {
                    outRequest.close();
                }
                    UsbRequest inRequest = new UsbRequest();
                    ByteBuffer inBuffer = ByteBuffer.allocate(expectedResponseSize).order(ByteOrder.LITTLE_ENDIAN);
                    if (!inRequest.initialize(conn, in))
                        throw new NXTException("Can not initialize IN request");
                    try {
                        if (!inRequest.queue(inBuffer, expectedResponseSize))
                            throw new NXTException("Can not queue IN request");
                        UsbRequest usbRequest = conn.requestWait();
                        if (usbRequest == null)
                            throw new NXTException("IN Request wait error");
                        inBuffer.rewind();
                        return inBuffer;
                    } finally {
                        inRequest.close();
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