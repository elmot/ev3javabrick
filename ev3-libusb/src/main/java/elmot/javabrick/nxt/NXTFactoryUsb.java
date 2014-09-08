package elmot.javabrick.nxt;

import elmot.javabrick.ev3.EV3;
import elmot.javabrick.ev3.impl.EV3Usb;
import elmot.javabrick.nxt.impl.NXTUsb;

import javax.usb.*;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author elmot
 */
public class NXTFactoryUsb {

    private static void findDevices(UsbHub hub, short vendorId, short productId, List<NXT> targetList) {
        //noinspection unchecked
        for (UsbDevice device : (List<UsbDevice>) hub.getAttachedUsbDevices()) {
            UsbDeviceDescriptor desc = device.getUsbDeviceDescriptor();
            if (desc.idVendor() == vendorId && desc.idProduct() == productId) {
                UsbInterface usbInterface = device.getActiveUsbConfiguration().getUsbInterface((byte) 0);
                targetList.add(new NXTUsb(usbInterface));
            }
            if (device.isUsbHub()) {
                findDevices((UsbHub) device, vendorId, productId, targetList);
            }
        }
    }

    public NXTFactoryUsb() throws SocketException {
    }

    static public synchronized List<NXT> listDiscovered() {
        ArrayList<NXT> nxt3s = new ArrayList<NXT>();
        try {
            findDevices(UsbHostManager.getUsbServices().getRootUsbHub(), (short) 0x0694, (short) 0x0002, nxt3s);
        } catch (UsbException e) {
            throw new RuntimeException(e);
        }
        return nxt3s;
    }

}
