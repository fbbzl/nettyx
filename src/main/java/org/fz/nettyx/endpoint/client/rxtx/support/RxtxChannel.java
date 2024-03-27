package org.fz.nettyx.endpoint.client.rxtx.support;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import org.fz.nettyx.channel.SerialCommPortChannel;

import java.net.SocketAddress;

import static org.fz.nettyx.endpoint.client.rxtx.support.RxtxChannelOption.*;


/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/3/4 19:29
 */

public class RxtxChannel extends SerialCommPortChannel {

    private static final RxtxDeviceAddress LOCAL_ADDRESS = new RxtxDeviceAddress("localhost");

    private final RxtxChannelConfig config;

    private RxtxDeviceAddress deviceAddress;
    private SerialPort        serialPort;

    public RxtxChannel() {
        config = new DefaultRxtxChannelConfig(this);
    }

    @Override
    public RxtxChannelConfig config() {
        return config;
    }

    @Override
    protected void doConnect(SocketAddress remoteAddress, SocketAddress localAddress) throws Exception {
        RxtxDeviceAddress        remote   = (RxtxDeviceAddress) remoteAddress;
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
    public RxtxDeviceAddress localAddress() {
        return (RxtxDeviceAddress) super.localAddress();
    }

    @Override
    public RxtxDeviceAddress remoteAddress() {
        return (RxtxDeviceAddress) super.remoteAddress();
    }

    @Override
    protected RxtxDeviceAddress localAddress0() {
        return LOCAL_ADDRESS;
    }

    @Override
    protected RxtxDeviceAddress remoteAddress0() {
        return deviceAddress;
    }

    @Override
    protected boolean isInputShutdown() {
        return !open;
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
