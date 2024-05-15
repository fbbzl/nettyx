package org.fz.nettyx.channel;

import java.net.SocketAddress;

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
