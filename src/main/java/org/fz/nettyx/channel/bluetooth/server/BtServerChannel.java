package org.fz.nettyx.channel.bluetooth.server;

import io.netty.channel.ChannelConfig;
import io.netty.channel.ServerChannel;
import org.fz.nettyx.channel.EnhancedOioByteStreamChannel;

import java.net.SocketAddress;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/5/15 17:29
 */
public class BtServerChannel extends EnhancedOioByteStreamChannel implements ServerChannel{

    @Override
    protected boolean isInputShutdown() {
        return false;
    }

    @Override
    protected void doConnect(SocketAddress remoteAddress, SocketAddress localAddress) throws Exception {

    }

    @Override
    protected SocketAddress localAddress0() {
        return null;
    }

    @Override
    protected SocketAddress remoteAddress0() {
        return null;
    }

    @Override
    protected void doBind(SocketAddress localAddress) throws Exception {

    }

    @Override
    public ChannelConfig config() {
        return null;
    }

    @Override
    public boolean isOpen() {
        return false;
    }
}
