package org.fz.nettyx.endpoint.client.jsc.support;


import static org.fz.nettyx.endpoint.client.jsc.support.JscChannelOption.BAUD_RATE;
import static org.fz.nettyx.endpoint.client.jsc.support.JscChannelOption.DATA_BITS;
import static org.fz.nettyx.endpoint.client.jsc.support.JscChannelOption.DTR;
import static org.fz.nettyx.endpoint.client.jsc.support.JscChannelOption.PARITY_BIT;
import static org.fz.nettyx.endpoint.client.jsc.support.JscChannelOption.READ_TIMEOUT;
import static org.fz.nettyx.endpoint.client.jsc.support.JscChannelOption.RTS;
import static org.fz.nettyx.endpoint.client.jsc.support.JscChannelOption.STOP_BITS;
import static org.fz.nettyx.endpoint.client.jsc.support.JscChannelOption.WAIT_TIME;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortTimeoutException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;
import io.netty.channel.oio.OioByteStreamChannel;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;


/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/3/2 13:29
 */

@SuppressWarnings("deprecation")
public class JscChannel extends OioByteStreamChannel {

    private static final JscDeviceAddress LOCAL_ADDRESS = new JscDeviceAddress("localhost");

    private final JscChannelConfig config;

    private boolean          open = true;
    private JscDeviceAddress deviceAddress;
    private SerialPort       serialPort;

    public JscChannel() {
        super(null);

        config = new DefaultJscChannelConfig(this);
    }

    @Override
    public JscChannelConfig config() {
        return config;
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    protected AbstractUnsafe newUnsafe() {
        return new JSerialCommUnsafe();
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
    protected void doBind(SocketAddress localAddress) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void doDisconnect() throws Exception {
        doClose();
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

    @Override
    protected boolean isInputShutdown() {
        return !open;
    }

    @Override
    protected ChannelFuture shutdownInput() {
        return newFailedFuture(new UnsupportedOperationException("shutdownInput"));
    }

    private final class JSerialCommUnsafe extends AbstractUnsafe {

        @Override
        public void connect(
            final SocketAddress remoteAddress,
            final SocketAddress localAddress, final ChannelPromise promise) {
            if (!promise.setUncancellable() || !ensureOpen(promise)) {
                return;
            }

            try {
                final boolean wasActive = isActive();
                doConnect(remoteAddress, localAddress);

                int waitTime = config().getOption(WAIT_TIME);
                if (waitTime > 0) {
                    eventLoop().schedule(() -> {
                        try {
                            doInit();
                            safeSetSuccess(promise);
                            if (!wasActive && isActive()) {
                                pipeline().fireChannelActive();
                            }
                        }
                        catch (Throwable t) {
                            safeSetFailure(promise, t);
                            closeIfClosed();
                        }
                    }, waitTime, TimeUnit.MILLISECONDS);
                } else {
                    doInit();
                    safeSetSuccess(promise);
                    if (!wasActive && isActive()) {
                        pipeline().fireChannelActive();
                    }
                }
            }
            catch (Throwable t) {
                safeSetFailure(promise, t);
                closeIfClosed();
            }
        }
    }
}