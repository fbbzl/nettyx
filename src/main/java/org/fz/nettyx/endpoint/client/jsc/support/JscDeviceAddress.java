package org.fz.nettyx.endpoint.client.jsc.support;


import java.net.SocketAddress;

/**
 * A {@link SocketAddress} subclass to wrap the serial port address of a jSerialComm device like COM1, /dev/ttyUSB0
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2024/3/2 13:29
 */
public class JscDeviceAddress extends SocketAddress {

    private static final long   serialVersionUID = -1205670554044857244L;
    private final        String value;

    /**
     * Creates a RxtxDeviceAddress representing the address of the serial port.
     *
     * @param value the address of the device (e.g. COM1, /dev/ttyUSB0, ...)
     */
    public JscDeviceAddress(String value) {
        this.value = value;
    }

    /**
     * @return The serial port address of the device (e.g. COM1, /dev/ttyUSB0, ...)
     */
    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return "JscDeviceAddress{" +
               "value='" + value + '\'' +
               '}';
    }
}