package client.bluetooth;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.LocalDevice;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/5/15 17:42
 */
public class TestBluetooth {


    public static void main(String[] args) throws BluetoothStateException {
        LocalDevice localDevice = LocalDevice.getLocalDevice();
        System.err.println(localDevice);
    }
}
