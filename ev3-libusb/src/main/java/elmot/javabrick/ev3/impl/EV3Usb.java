package elmot.javabrick.ev3.impl;

import elmot.javabrick.ev3.EV3;

import javax.usb.UsbEndpoint;
import javax.usb.UsbException;
import javax.usb.UsbInterface;
import javax.usb.UsbPipe;
import java.io.IOException;
import java.util.Arrays;

public class EV3Usb extends EV3 {

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
    public synchronized byte[] dataExchange(byte[] command) throws IOException {
        try {
            brick.claim();
            try {
                UsbEndpoint endpointIn = brick.getUsbEndpoint((byte) 0x81);
                UsbEndpoint endpointOut = brick.getUsbEndpoint((byte) 0x1);
                UsbPipe pipeIn = endpointIn.getUsbPipe();
                UsbPipe pipeOut = endpointOut.getUsbPipe();
                pipeOut.open();
                System.arraycopy(command, 0, dataBlock, 0, command.length);
                try {
                    pipeOut.syncSubmit(dataBlock);
                } finally {
                    pipeOut.close();
                }
                pipeIn.open();
                try {
                    pipeIn.syncSubmit(dataBlock);
                    int length = 2 + (0xff & (int) dataBlock[0]) + (dataBlock[1] << 8);
                    return Arrays.copyOfRange(dataBlock, 0, length);
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
