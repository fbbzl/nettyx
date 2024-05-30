package org.fz.nettyx.channel.bluetooth;

import lombok.SneakyThrows;

import javax.bluetooth.RemoteDevice;
import java.io.IOException;
import java.net.SocketAddress;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/5/26 10:32
 */
public class BtDeviceAddress extends SocketAddress {

    private final String value;
    private final String friendlyName;

    @SneakyThrows(IOException.class)
    public BtDeviceAddress(RemoteDevice device) {
        this.value        = device.getBluetoothAddress();
        this.friendlyName = device.getFriendlyName(false);
    }

    public BtDeviceAddress(String value) {
        this.value = this.friendlyName = value;
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return friendlyName;
    }

}
