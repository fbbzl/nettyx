package org.fz.nettyx.endpoint.client.jsc.support;


import com.fazecast.jSerialComm.SerialPort;
import io.netty.channel.ChannelConfig;
import org.fz.nettyx.channel.SerialCommChannel;

import java.net.SocketAddress;

import static org.fz.nettyx.endpoint.client.jsc.support.JscChannelOption.*;


/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/3/2 13:29
 */

public class JscChannel extends SerialCommChannel {

    private final JscChannelConfig config;
    private       SerialPort       serialPort;

    public JscChannel() {
        config = new DefaultJscChannelConfig(this);
    }

    @Override
    protected int waitTime(ChannelConfig config) {
        return config().getOption(WAIT_TIME);
    }

    @Override
    public JscChannelConfig config() {
        return config;
    }

    @Override
    protected void doConnect(SocketAddress remoteAddress, SocketAddress localAddress) {
        SerialCommAddress remote   = (SerialCommAddress) remoteAddress;
        SerialPort        commPort = SerialPort.getCommPort(remote.value());

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