package elmot.javabrick.ev3.impl;

import elmot.javabrick.ev3.EV3;

import javax.usb.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.logging.Logger;

public class EV3Usb extends EV3 implements UsbInterfacePolicy {

    public static final int EV3_USB_BLOCK_SIZE = 1024;
    public static final Logger LOGGER = Logger.getLogger(EV3Usb.class.getName());
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
                command.get(dataBlock,0,command.limit());
                try {
                    pipeOut.syncSubmit(dataBlock);
                } finally {
                    pipeOut.close();
                }
                pipeIn.open();
                try {
                    while (true) {
                        pipeIn.syncSubmit(dataBlock);
                        int length = 2 + (0xff & (int) dataBlock[0]) + (dataBlock[1] << 8);
                        if (length < 3 || length > 1022) {
                            LOGGER.warning("Garbage in USB queue - skipping");
                            continue;
                        }
                        ByteBuffer response = ByteBuffer.allocate(length).order(ByteOrder.LITTLE_ENDIAN);
                        response.put(dataBlock, 0, length);
                        int readSeqNo = response.getShort(2);
                        if (readSeqNo < expectedSeqNo) {
                            LOGGER.warning("Resynch EV3 seq no");
                            continue;
                        }
                        return response;
                    }
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
