package org.fz.nettyx.channel;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelFuture;
import io.netty.channel.oio.AbstractOioChannel;
import io.netty.channel.oio.OioByteStreamChannel;
import io.netty.util.concurrent.DefaultEventExecutor;

import java.net.SocketAddress;


/**
 * cause of {@link AbstractOioChannel sync blocking read task}
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2024/3/27 14:27
 */

@SuppressWarnings("deprecation")
public abstract class ReadAsyncOioByteStreamChannel extends OioByteStreamChannel {

    private final DefaultEventExecutor eventExecutors = new DefaultEventExecutor();

    protected ReadAsyncOioByteStreamChannel() {
        super(null);
    }

    protected boolean open = true;

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    protected boolean isInputShutdown() {
        return !open;
    }

    @Override
    protected int doReadBytes(ByteBuf buf) {
        try {
            return super.doReadBytes(buf);
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public void doRead() {
        // do not use method reference!!!
        Runnable runnable = () -> ReadAsyncOioByteStreamChannel.super.doRead();
        eventExecutors.execute(runnable);
    }

    @Override
    protected void doClose() throws Exception {
        try {
            super.doClose();
        } finally {
            this.eventExecutors.shutdownGracefully();
        }
    }

    @Override
    protected void doDisconnect() throws Exception {
        doClose();
    }

    @Override
    protected ChannelFuture shutdownInput() {
        return newFailedFuture(new UnsupportedOperationException("shutdownInput"));
    }

    @Override
    protected void doBind(SocketAddress localAddress) {
        throw new UnsupportedOperationException("doBind");
    }

    protected abstract void doInit();

    protected abstract int waitTime(ChannelConfig config);

}
