package org.fz.nettyx.endpoint.serial;

import lombok.RequiredArgsConstructor;

import java.net.SocketAddress;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/5/15 12:44
 */
@RequiredArgsConstructor
public class SerialCommAddress extends SocketAddress {

    private static final long   serialVersionUID = -870353013039000250L;
    private final        String value;

    /**
     * @return The serial port address of the device (e.g. COM1, /dev/ttyUSB0, ...)
     */
    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return "SerialCommPortAddress{" +
               "value='" + value + '\'' +
               '}';
    }
}
