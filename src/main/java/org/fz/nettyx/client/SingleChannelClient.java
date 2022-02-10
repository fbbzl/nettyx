package org.fz.nettyx.client;


import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;
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
public abstract class SingleChannelClient extends TcpClient {

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
        if (gracefullyCloseable(channel)) {
            this.closeChannel();
        }
    }

    public void closeChannelGracefully(ChannelPromise promise) {
        if (gracefullyCloseable(channel)) {
            this.closeChannel(promise);
        }
    }

    /**
     * Connect.
     *
     * @param address the address
     */
    public abstract void connect(SocketAddress address);

    public void send(Object message) {
        if (this.notReady(channel)) {
            log.debug("connection have not been initialized, message will be discard: {}", message);
            return;
        }

        try {
            channel.writeAndFlush(message);

            log.debug("has send message to: [{}]", channel.remoteAddress());
        } catch (Exception exception) {
            log.error("exception occurred while sending the message [" + message + "], remote address is [" + channel.remoteAddress() + "]", exception);
        }
    }
}
