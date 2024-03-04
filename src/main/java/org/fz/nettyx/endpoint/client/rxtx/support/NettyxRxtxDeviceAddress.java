package org.fz.nettyx.endpoint.client.rxtx.support;

import io.netty.channel.rxtx.RxtxDeviceAddress;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/3/4 20:47
 */

@SuppressWarnings("deprecation")
public class NettyxRxtxDeviceAddress extends RxtxDeviceAddress {

    /**
     * Creates a RxtxDeviceAddress representing the address of the serial port.
     *
     * @param value the address of the device (e.g. COM1, /dev/ttyUSB0, ...)
     */
    public NettyxRxtxDeviceAddress(String value) {
        super(value);
    }

    @Override
    public String toString() {
        return "RxtxDeviceAddress{" +
               "value='" + value() + '\'' +
               '}';
    }
}
