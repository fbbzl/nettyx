package org.fz.nettyx.channel.jsc;


import com.fazecast.jSerialComm.SerialPort;
import org.fz.nettyx.channel.SerialCommChannel;
import org.fz.nettyx.util.Throws;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketAddress;


/**
 * java serial comm channel
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2024/3/2 13:29
 */

public class JscChannel extends SerialCommChannel {

    private final JscChannelConfig config;
    private       SerialPort       serialPort;

    public JscChannel() {
        config = new JscChannelConfig.DefaultJscChannelConfig(this);
    }

    @Override
    protected int available() {
        return serialPort.bytesAvailable();
    }

    @Override
    public JscChannelConfig config() {
        return config;
    }

    @Override
    protected void doConnect(SocketAddress remoteAddress, SocketAddress localAddress) throws Exception {
        // always check before do connect
        if (this.serialPort != null && this.serialPort.isOpen()) this.doClose();

        this.remoteAddress = (SerialCommAddress) remoteAddress;
        this.serialPort    = SerialPort.getCommPort(this.remoteAddress.value());

        // check comm-port
        Throws.ifFalse(this.serialPort.openPort(), new IllegalArgumentException("Unable to open [" + this.remoteAddress.value() + "] port"));

        // set config
        this.serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, config().getOption(JscChannelOption.READ_TIMEOUT), 0);
        this.serialPort.setComPortParameters(
                config().getOption(JscChannelOption.BAUD_RATE),
                config().getOption(JscChannelOption.DATA_BITS).value(),
                config().getOption(JscChannelOption.STOP_BITS).value(),
                config().getOption(JscChannelOption.PARITY_BIT).value());

        if (Boolean.TRUE.equals(config().getOption(JscChannelOption.DTR))) serialPort.setDTR();
        if (Boolean.TRUE.equals(config().getOption(JscChannelOption.RTS))) serialPort.setRTS();
    }

    @Override
    protected InputStream getInputStream() {
        return serialPort.getInputStream();
    }

    @Override
    protected OutputStream getOutputStream() {
        return serialPort.getOutputStream();
    }

    @Override
    protected void doClose() throws Exception {
        try {
            super.doClose();
        } finally {
            if (serialPort != null) {
                serialPort.removeDataListener();
                serialPort.closePort();
                serialPort = null;
            }
        }
    }

}