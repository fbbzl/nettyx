package org.fz.nettyx.channel.bluetooth;

import java.net.SocketAddress;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/5/26 10:32
 */
public class BtDeviceAddress extends SocketAddress {

    private final String value;
    private final String friendlyName;

    public BtDeviceAddress(String value, String friendlyName)
    {
        this.value        = value;
        this.friendlyName = friendlyName;
    }

    public BtDeviceAddress(String value)
    {
        this.value = this.friendlyName = value;
    }

    public String value()
    {
        return value;
    }

    @Override
    public String toString()
    {
        return friendlyName;
    }

}
