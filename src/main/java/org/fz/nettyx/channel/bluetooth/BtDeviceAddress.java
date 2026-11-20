package org.fz.nettyx.channel.bluetooth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.net.SocketAddress;

/**
 * Represents a Bluetooth device address for a Netty channel.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2024/5/26 10:32
 */
@RequiredArgsConstructor
public class BtDeviceAddress extends SocketAddress {

    @Getter
    @Accessors(fluent = true)
    private final String value;
    private final String friendlyName;

    public BtDeviceAddress(String value)
    {
        this.value = this.friendlyName = value;
    }

    @Override
    public String toString()
    {
        return String.valueOf(friendlyName);
    }

}
