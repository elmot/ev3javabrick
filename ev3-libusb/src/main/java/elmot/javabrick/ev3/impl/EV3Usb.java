package elmot.javabrick.ev3.impl;

import elmot.javabrick.ev3.EV3;

import javax.usb.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class EV3Usb extends EV3 implements UsbInterfacePolicy {

    public static final int EV3_USB_BLOCK_SIZE = 1024;
    private final UsbInterface brick;
    private byte[] dataBlock = new byte[EV3_USB_BLOCK_SIZE];

    public EV3Usb(UsbInterface brick) {
        this.brick = brick;
    }

    @Override
    public void ensureOpen() throws IOException {
//        if (brick.isActive()) throw new IOException("Brick is not active. Disconnected?");
    }

    @Override
    public void close() throws Exception {
    }

    @Override
    public boolean forceClaim(UsbInterface usbInterface) {
        return true;
    }

    @Override
    public ByteBuffer dataExchange(ByteBuffer command, int expectedSeqNo) throws IOException {
        try {
            brick.claim(this);
            try {
                UsbEndpoint endpointIn = brick.getUsbEndpoint((byte) 0x81);
                UsbEndpoint endpointOut = brick.getUsbEndpoint((byte) 0x1);
                UsbPipe pipeIn = endpointIn.getUsbPipe();
                UsbPipe pipeOut = endpointOut.getUsbPipe();
                pipeOut.open();
                command.rewind();
                command.get(dataBlock);
                try {
                    pipeOut.syncSubmit(dataBlock);
                } finally {
                    pipeOut.close();
                }
                pipeIn.open();
                try {
                    pipeIn.syncSubmit(dataBlock);
                    int length = 2 + (0xff & (int) dataBlock[0]) + (dataBlock[1] << 8);
                    ByteBuffer response = ByteBuffer.allocate(length).order(ByteOrder.LITTLE_ENDIAN);
                    response.put(dataBlock, 0, length);
                    return response;
                } finally {
                    pipeIn.close();
                }
            } finally {
                brick.release();
            }
        } catch (UsbException e) {
            throw new IOException(e);
        }
    }
}
