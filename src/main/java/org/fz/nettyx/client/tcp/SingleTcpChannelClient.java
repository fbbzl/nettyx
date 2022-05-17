package org.fz.nettyx.client.tcp;


import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;
import io.netty.channel.DefaultChannelPromise;
import java.net.SocketAddress;
import lombok.extern.slf4j.Slf4j;

/**
 * Single channel client
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2021 /5/6 15:22
 */
@Slf4j
public abstract class SingleTcpChannelClient extends TcpClient {

    protected Channel channel;

    protected void storeChannel(ChannelFuture cf) {
        this.channel = cf.channel();
    }

    public Channel getChannel() {
        return this.channel;
    }

    public void closeChannel() {
        this.channel.close();
    }

    public void closeChannel(ChannelPromise promise) {
        this.channel.close(promise);
    }

    public void closeChannelGracefully() {
        if (gracefullyCloseable(channel)) this.closeChannel();
    }

    public void closeChannelGracefully(ChannelPromise promise) {
        if (gracefullyCloseable(channel)) this.closeChannel(promise);
    }

    /**
     * Connect.
     *
     * @param address the address
     */
    public abstract void connect(SocketAddress address);

    public ChannelPromise send(Object message) {
        if (this.inActive(channel)) {
            log.debug("channel not in active status, message will be discard: {}", message);
            return channel == null ? null : new DefaultChannelPromise(channel).setFailure(new ChannelException("channel: [" + channel + "] is not usable"));
        }

        try {
            return (ChannelPromise) channel.writeAndFlush(message);
        } catch (Exception exception) {
            throw new ChannelException("exception occurred while sending the message [" + message + "], remote address is [" + channel.remoteAddress() + "]", exception);
        }
    }
}
