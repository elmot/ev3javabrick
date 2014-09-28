package elmot.javabrick.nxt.usb;


import elmot.javabrick.nxt.NXT;
import elmot.javabrick.nxt.NXTException;
import elmot.javabrick.nxt.NXTFactoryUsb;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import static elmot.javabrick.nxt.NXT.Motor.*;
/**
 * @author elmot
 *         Date: 06.09.14
 */
//@Ignore
public class TestNxtUsb {
    public static final String TEST_TEXT = "LAMBADA!!!~";
    private NXT NXT;
    /*
    @Test
    public void testBatteryBeep() throws UsbException, UnsupportedEncodingException {
        UsbDevice nxt = findNxt();
        assertNotNull(nxt);
        UsbInterface usbInterface = nxt.getUsbConfiguration((byte) 1).getUsbInterface((byte) 0);
        usbInterface.claim(new UsbInterfacePolicy() {
            @Override
            public boolean forceClaim(UsbInterface usbInterface) {
                return true;
            }
        });
        try {
            UsbEndpoint endpointOut = usbInterface.getUsbEndpoint((byte) 1);
            UsbEndpoint endpointIn = usbInterface.getUsbEndpoint((byte) 0x82);
            UsbPipe usbPipeOut = endpointOut.getUsbPipe();
            UsbPipe usbPipeIn = endpointIn.getUsbPipe();
            usbPipeOut.open();
            try {
//                usbPipeOut.syncSubmit(new byte[]{0x1, (byte) 0x9b});//GETDEVICEINFO
//                usbPipeOut.syncSubmit(new byte[]{0x0, (byte) 0xb});//GETBATTERYVOLTAGE
                usbPipeOut.syncSubmit(new byte[]{0x0, (byte) 3,2,2,2,2});//tone
            } finally {
                usbPipeOut.close();
            }
            usbPipeIn.open();
            try {
                byte[] reply = new byte[5];
                usbPipeIn.syncSubmit(reply);
                System.out.println("Arrays.toString(reply) = " + Arrays.toString(reply));
            } finally {
                usbPipeIn.close();
            }
//        endpointOut.getUsbPipe().
        } finally {
            usbInterface.release();
        }
    }
*/

/*
    private UsbDevice findNxt() throws UsbException {
        return findDevice(UsbHostManager.getUsbServices().getRootUsbHub());
    }

    private UsbDevice findDevice(UsbHub rootUsbHub) {
        List<UsbDevice> devices = rootUsbHub.getAttachedUsbDevices();
        for (UsbDevice device : devices) {
            if (device.isUsbHub()) {
                UsbDevice subDevice = findDevice((UsbHub) device);
                if (subDevice != null) return subDevice;
            } else {
                UsbDeviceDescriptor usbDeviceDescriptor = device.getUsbDeviceDescriptor();
                if (usbDeviceDescriptor.idVendor() == 0x0694 && usbDeviceDescriptor.idProduct() == 2) {
                    return device;
                }
            }
        }
        return null;
    }
*/

    @Before
    public void setup() {
        NXT = NXTFactoryUsb.listDiscovered().get(0);
    }

    @Test
    public void testBeep() throws IOException {
        NXT.SYSTEM.playTone(300, 300);
    }

    @Test
    public void testVoltage() throws IOException {
        double batteryLevel = NXT.SYSTEM.getVBatt();
        System.out.println("batteryLevel = " + batteryLevel);
        assertTrue(batteryLevel > 5);
        assertTrue(batteryLevel < 11);
    }

    @Test
    public void testMessage() throws IOException, InterruptedException {
        NXT.SYSTEM.startProgram("MSG_TEXT.rxe");
        for(int i =0; i <  20; i++)
        {
            if(NXT.SYSTEM.messageRead((byte)3, (byte) 0,true) == null) {
                break;
            }
        }
        NXT.SYSTEM.messageWrite((byte) 3, TEST_TEXT);
        Thread.sleep(500);
        String s = NXT.SYSTEM.messageRead((byte)3, (byte) 0, true);
        assertEquals(TEST_TEXT,s);
    }

    @Test
    public void testMotors() throws InterruptedException, NXTException {
        NXT.MOTOR.resetMotorPosition(A,true);
        NXT.MOTOR.resetMotorPosition(B,true);
        NXT.MOTOR.resetMotorPosition(B,false);
        NXT.MOTOR.resetMotorPosition(A,false);
        assertEquals(0, NXT.MOTOR.getAbsoluteTacho(A));
        NXT.MOTOR.steerMotors(A, B, -40, 60, 0);
        Thread.sleep (5000);
        NXT.MOTOR.stopMotors();
        System.out.println("NXT.MOTOR.getAbsoluteTacho(A) = " + NXT.MOTOR.getAbsoluteTacho(A));

    }
}

