package elmot.javabrick.ev3;

import elmot.javabrick.ev3.impl.EV3Usb;

import javax.usb.*;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author elmot
 */
public class EV3FactoryUsb {

    private static void findDevices(UsbHub hub, short vendorId, short productId, List<EV3> targetList) {
        //noinspection unchecked
        for (UsbDevice device : (List<UsbDevice>) hub.getAttachedUsbDevices()) {
            UsbDeviceDescriptor desc = device.getUsbDeviceDescriptor();
            if (desc.idVendor() == vendorId && desc.idProduct() == productId) {
                UsbInterface usbInterface = device.getActiveUsbConfiguration().getUsbInterface((byte) 0);
                targetList.add(new EV3Usb(usbInterface));
            }
            if (device.isUsbHub()) {
                findDevices((UsbHub) device, vendorId, productId, targetList);
            }
        }
    }

    public EV3FactoryUsb() throws SocketException {
    }

    static public synchronized List<EV3> listDiscovered() {
        ArrayList<EV3> ev3s = new ArrayList<EV3>();
        try {
            findDevices(UsbHostManager.getUsbServices().getRootUsbHub(), (short) 0x0694, (short) 0x0005, ev3s);
        } catch (UsbException e) {
            throw new RuntimeException(e);
        }
        return ev3s;
    }

}
