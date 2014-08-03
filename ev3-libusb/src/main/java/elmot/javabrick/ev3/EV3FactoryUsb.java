package elmot.javabrick.ev3;

import elmot.javabrick.ev3.impl.EV3BrickUsb;

import javax.usb.*;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author elmot
 */
public class EV3FactoryUsb {

    private static void findDevices(UsbHub hub, short vendorId, short productId, List<EV3Brick> targetList) {
        //noinspection unchecked
        for (UsbDevice device : (List<UsbDevice>) hub.getAttachedUsbDevices()) {
            UsbDeviceDescriptor desc = device.getUsbDeviceDescriptor();
            if (desc.idVendor() == vendorId && desc.idProduct() == productId) {
                UsbInterface usbInterface = device.getActiveUsbConfiguration().getUsbInterface((byte) 0);
                targetList.add(new EV3BrickUsb(usbInterface));
            }
            if (device.isUsbHub()) {
                findDevices((UsbHub) device, vendorId, productId, targetList);
            }
        }
    }

    public EV3FactoryUsb() throws SocketException {
    }

    static public synchronized List<EV3Brick> listDiscovered() throws UsbException {
        ArrayList<EV3Brick> ev3Bricks = new ArrayList<EV3Brick>();
        findDevices(UsbHostManager.getUsbServices().getRootUsbHub(), (short) 0x0694, (short) 0x0005, ev3Bricks);
        return ev3Bricks;
    }

}
