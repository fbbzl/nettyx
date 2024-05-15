package org.fz.nettyx.endpoint.serial.rxtx.support;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;
import io.netty.channel.ChannelConfig;
import org.fz.nettyx.channel.SerialCommChannel;

import java.io.IOException;
import java.net.SocketAddress;


/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/3/4 19:29
 */

public class RxtxChannel extends SerialCommChannel {

    private final RxtxChannelConfig config;
    private       SerialPort        serialPort;

    public RxtxChannel() {
        config = new DefaultRxtxChannelConfig(this);
    }

    @Override
    protected int waitTime(ChannelConfig config) {
        return config.getOption(RxtxChannelOption.WAIT_TIME);
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
        commPort.enableReceiveTimeout(config().getOption(RxtxChannelOption.READ_TIMEOUT));

        deviceAddress = remote;
        serialPort    = (SerialPort) commPort;
    }

    @Override
    protected void doInit() {
        try {
            serialPort.setSerialPortParams(
                    config().getOption(RxtxChannelOption.BAUD_RATE),
                    config().getOption(RxtxChannelOption.DATA_BITS).value(),
                    config().getOption(RxtxChannelOption.STOP_BITS).value(),
                    config().getOption(RxtxChannelOption.PARITY_BIT).value()
                                          );
            serialPort.setDTR(config().getOption(RxtxChannelOption.DTR));
            serialPort.setRTS(config().getOption(RxtxChannelOption.RTS));

            activate(serialPort.getInputStream(), serialPort.getOutputStream());
        } catch (UnsupportedCommOperationException commPortError) {
            throw new IllegalArgumentException("can not open comm port", commPortError);
        } catch (IOException steamError) {
            throw new UnsupportedOperationException("can get input/output stream, please check", steamError);
        }
    }

    @Override
    protected void doClose() throws Exception {
        open = false;
        try {
            super.doClose();
        } finally {
            if (serialPort != null) {
                serialPort.removeEventListener();
                serialPort.close();
                serialPort = null;
            }
        }

    }
}
