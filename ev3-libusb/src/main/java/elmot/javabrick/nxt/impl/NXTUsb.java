package elmot.javabrick.nxt.impl;

import elmot.javabrick.nxt.NXT;
import elmot.javabrick.nxt.NXTException;

import javax.usb.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @author elmot
 *         Date: 07.09.14
 */
public class NXTUsb extends NXT implements UsbInterfacePolicy {

    private final UsbInterface usbInterface;

    public NXTUsb(UsbInterface usbInterface) {
        this.usbInterface = usbInterface;
    }

    @Override
    public void ensureOpen() throws NXTException {
        try {
            usbInterface.claim(this);
        } catch (UsbException e) {
            throw new NXTException(e);
        }
    }

    @Override
    public void close() {
        try {
            usbInterface.release();
        } catch (UsbException ignored) {
        }
    }

    @Override
    public boolean forceClaim(UsbInterface usbInterface) {
        return true;
    }

    @Override
    public ByteBuffer dataExchange(ByteBuffer cmd, int expectedResponseSize) throws NXTException {
        UsbEndpoint endpointOut = usbInterface.getUsbEndpoint((byte) 1);
        UsbEndpoint endpointIn = usbInterface.getUsbEndpoint((byte) 0x82);
        UsbPipe usbPipeOut = endpointOut.getUsbPipe();
        UsbPipe usbPipeIn = endpointIn.getUsbPipe();
        try {
            usbPipeOut.open();
            try {
                byte[] bytes = new byte[cmd.limit()];
                cmd.get(bytes,0,bytes.length);
                usbPipeOut.syncSubmit(bytes);
            } finally {
                usbPipeOut.close();
            }
            usbPipeIn.open();
            try {
                byte[] reply = new byte[expectedResponseSize];
                usbPipeIn.syncSubmit(reply);
                return ByteBuffer.wrap(reply).order(ByteOrder.LITTLE_ENDIAN);
            } finally {
                usbPipeIn.close();
            }

        } catch (UsbException e) {
            throw new NXTException(e);
        }
    }
}
