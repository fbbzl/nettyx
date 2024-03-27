package org.fz.nettyx.endpoint.client.jsc.support;


import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortTimeoutException;
import io.netty.buffer.ByteBuf;
import org.fz.nettyx.channel.SerialCommPortChannel;

import java.net.SocketAddress;

import static org.fz.nettyx.endpoint.client.jsc.support.JscChannelOption.*;


/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/3/2 13:29
 */

public class JscChannel extends SerialCommPortChannel {

    private static final JscDeviceAddress LOCAL_ADDRESS = new JscDeviceAddress("localhost");

    private final JscChannelConfig config;
    private       JscDeviceAddress deviceAddress;
    private       SerialPort       serialPort;

    public JscChannel() {
        config = new DefaultJscChannelConfig(this);
    }

    @Override
    public JscChannelConfig config() {
        return config;
    }

    @Override
    protected void doConnect(SocketAddress remoteAddress, SocketAddress localAddress) {
        JscDeviceAddress remote   = (JscDeviceAddress) remoteAddress;
        SerialPort       commPort = SerialPort.getCommPort(remote.value());

        if (!commPort.openPort()) {
            throw new IllegalArgumentException("Unable to open [" + remote.value() + "] port");
        }
        commPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, config().getOption(READ_TIMEOUT), 0);

        deviceAddress = remote;
        serialPort    = commPort;
    }

    protected void doInit() {
        serialPort.setComPortParameters(
                config().getOption(BAUD_RATE),
                config().getOption(DATA_BITS),
                config().getOption(STOP_BITS).value(),
                config().getOption(PARITY_BIT).value());

        if (Boolean.TRUE.equals(config().getOption(DTR))) {
            serialPort.setDTR();
        }
        if (Boolean.TRUE.equals(config().getOption(RTS))) {
            serialPort.setRTS();
        }

        activate(serialPort.getInputStream(), serialPort.getOutputStream());
    }

    @Override
    public JscDeviceAddress localAddress() {
        return (JscDeviceAddress) super.localAddress();
    }

    @Override
    public JscDeviceAddress remoteAddress() {
        return (JscDeviceAddress) super.remoteAddress();
    }

    @Override
    protected JscDeviceAddress localAddress0() {
        return LOCAL_ADDRESS;
    }

    @Override
    protected JscDeviceAddress remoteAddress0() {
        return deviceAddress;
    }

    @Override
    protected int doReadBytes(ByteBuf buf) throws Exception {
        try {
            return super.doReadBytes(buf);
        }
        catch (SerialPortTimeoutException e) {
            return 0;
        }
    }

    @Override
    protected void doClose() throws Exception {
        open = false;
        try {
            super.doClose();
        }
        finally {
            if (serialPort != null) {
                serialPort.removeDataListener();
                serialPort.closePort();
                serialPort = null;
            }
        }
    }

}