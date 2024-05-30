package org.fz.nettyx.channel.rxtx;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import org.fz.nettyx.channel.SerialCommChannel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketAddress;


/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/3/4 19:29
 */

public class RxtxChannel extends SerialCommChannel {

    private final RxtxChannelConfig config;
    private       SerialPort        serialPort;
    private boolean open;

    public RxtxChannel() {
        config = new RxtxChannelConfig.DefaultRxtxChannelConfig(this);
    }

    @Override
    public RxtxChannelConfig config() {
        return config;
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    protected boolean isInputShutdown() {
        return !isOpen();
    }

    @Override
    protected void doConnect(SocketAddress remoteAddress, SocketAddress localAddress) throws Exception {
        // always check before do connect
        if (this.serialPort != null) this.doClose();

        this.remoteAddress = (SerialCommAddress) remoteAddress;
        this.serialPort    = (SerialPort) CommPortIdentifier.getPortIdentifier(this.remoteAddress.value()).open(getClass().getName(), 1000);
        this.serialPort.enableReceiveTimeout(config().getOption(RxtxChannelOption.READ_TIMEOUT));

        serialPort.setSerialPortParams(
                config().getOption(RxtxChannelOption.BAUD_RATE),
                config().getOption(RxtxChannelOption.DATA_BITS).value(),
                config().getOption(RxtxChannelOption.STOP_BITS).value(),
                config().getOption(RxtxChannelOption.PARITY_BIT).value()
                                      );
        serialPort.setDTR(config().getOption(RxtxChannelOption.DTR));
        serialPort.setRTS(config().getOption(RxtxChannelOption.RTS));

        open = true;
    }

    @Override
    protected InputStream getInputStream() throws IOException {
        return serialPort.getInputStream();
    }

    @Override
    protected OutputStream getOutputStream() throws IOException {
        return serialPort.getOutputStream();
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
