package org.fz.nettyx.endpoint.client.rxtx.support;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import io.netty.channel.ChannelConfig;
import org.fz.nettyx.channel.SerialCommAddress;
import org.fz.nettyx.channel.SerialCommChannel;

import java.net.SocketAddress;

import static org.fz.nettyx.endpoint.client.rxtx.support.RxtxChannelOption.*;


/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/3/4 19:29
 */

public class RxtxChannel extends SerialCommChannel {

    private static final SerialCommAddress LOCAL_ADDRESS = new SerialCommAddress("localhost");

    private final RxtxChannelConfig config;
    private       SerialPort        serialPort;

    public RxtxChannel() {
        config = new DefaultRxtxChannelConfig(this);
    }

    @Override
    protected int waitTime(ChannelConfig config) {
        return config.getOption(WAIT_TIME);
    }

    @Override
    public RxtxChannelConfig config() {
        return config;
    }

    @Override
    protected void doConnect(SocketAddress remoteAddress, SocketAddress localAddress) throws Exception {
        SerialCommAddress        remote   = (SerialCommAddress) remoteAddress;
        final CommPortIdentifier cpi      = CommPortIdentifier.getPortIdentifier(remote.value());
        final CommPort           commPort = cpi.open(getClass().getName(), 1000);
        commPort.enableReceiveTimeout(config().getOption(READ_TIMEOUT));
        deviceAddress = remote;

        serialPort = (SerialPort) commPort;
    }

    @Override
    protected void doInit() throws Exception {
        serialPort.setSerialPortParams(
                config().getOption(BAUD_RATE),
                config().getOption(DATA_BITS).value(),
                config().getOption(STOP_BITS).value(),
                config().getOption(PARITY_BIT).value()
                                      );
        serialPort.setDTR(config().getOption(DTR));
        serialPort.setRTS(config().getOption(RTS));

        activate(serialPort.getInputStream(), serialPort.getOutputStream());
    }

    @Override
    public SerialCommAddress localAddress() {
        return (SerialCommAddress) super.localAddress();
    }

    @Override
    public SerialCommAddress remoteAddress() {
        return (SerialCommAddress) super.remoteAddress();
    }

    @Override
    protected SerialCommAddress localAddress0() {
        return LOCAL_ADDRESS;
    }

    @Override
    protected SerialCommAddress remoteAddress0() {
        return deviceAddress;
    }

    @Override
    protected void doClose() throws Exception {
        open = false;
        try {
            super.doClose();
        }
        finally {
            if (serialPort != null) {
                serialPort.removeEventListener();
                serialPort.close();
                serialPort = null;
            }
        }

    }
}
