package elmot.javabrick.nxt.android;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import elmot.javabrick.nxt.NXT;
import elmot.javabrick.nxt.NXTException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

public class NXTBluetoothAndroid extends NXT {
    public static final String LOG_TAG = "Bluetooth/NXT";
    private final BluetoothDevice device;
    private BluetoothSocket socket;
    private InputStream inputStream;
    private OutputStream outputStream;

    private static final UUID NXT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public NXTBluetoothAndroid(BluetoothDevice device) {
        this.device = device;
    }

    @Override
    public void ensureOpen() throws NXTException {
        BluetoothClass bluetoothClass = device.getBluetoothClass();
        if (bluetoothClass.getMajorDeviceClass() != BluetoothClass.Device.Major.TOY ||
                bluetoothClass.getDeviceClass() != BluetoothClass.Device.TOY_ROBOT)
            throw new NXTException("Wrong device class");
        try {
            socket = device.createInsecureRfcommSocketToServiceRecord(NXT_UUID);
            try {
                socket.connect();
            } catch (IOException e) {
                try {
                    Method mMethod = device.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
                    socket = (BluetoothSocket) mMethod.invoke(device, 1);
                    socket.connect();
                } catch (Exception e1) {
                    Log.e(LOG_TAG, "Bluetooth connect", e);
                }
            }
            outputStream = socket.getOutputStream();
            inputStream = socket.getInputStream();
        } catch (IOException e) {
            socket = null;
            inputStream = null;
            outputStream = null;
            Log.w(LOG_TAG, "Communication fault", e);
            throw new NXTException("Communication fault: " + e);
        }
    }

    @Override
    public void close() {
        if (inputStream != null) try {
            inputStream.close();
        } catch (Exception ignore) {
        }
        inputStream = null;
        if (outputStream != null) try {
            outputStream.close();
        } catch (Exception ignore) {
        }
        outputStream = null;
        if (socket != null) try {
            socket.close();
        } catch (Exception ignore) {
        }
        socket = null;

    }

    @Override
    public ByteBuffer dataExchange(ByteBuffer cmd, int expectedResponseSize) throws NXTException {
        try {
            outputStream.write(cmd.limit());
            outputStream.write(cmd.limit() / 256);
            outputStream.write(cmd.array(), cmd.arrayOffset(), cmd.limit());
            outputStream.flush();
            int length = inputStream.read();
            length |= inputStream.read() * 256;
            if (expectedResponseSize != length)
                throw new NXTException(String.format("Wrong reply packet length: %d instead of %d", length, expectedResponseSize));
            ByteBuffer result = ByteBuffer.allocate(length).order(ByteOrder.LITTLE_ENDIAN);
            while(length-- >=0)
            {
                result.put((byte) inputStream.read());
            }
            return (ByteBuffer) result.rewind();
        } catch (IOException e) {
            throw new NXTException(e);
        }
    }

}