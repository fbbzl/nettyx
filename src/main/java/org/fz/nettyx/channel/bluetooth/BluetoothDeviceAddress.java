package org.fz.nettyx.channel.bluetooth;

import java.net.SocketAddress;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/5/26 10:32
 */
public class BluetoothDeviceAddress extends SocketAddress {

    private final String value;

    public BluetoothDeviceAddress(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
