package org.fz.nettyx.client;


import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
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
public abstract class SingleChannelClient extends Client {

    protected Channel channel;

    protected void storeChannel(ChannelFuture cf) {
        this.channel = cf.channel();
    }

    /**
     * Connect.
     *
     * @param address the address
     */
    public abstract void connect(SocketAddress address);

    public void send(Object message) {
        try {
            if (this.notReady(channel)) {
                log.debug("connection have not been initialized, message will be discard: {}", message);
                return;
            }

            channel.writeAndFlush(message);

            log.debug("has send message to: [{}]", channel.remoteAddress());
        } catch (Exception exception) {
            log.error("exception occurred while sending the message", exception);
            this.shutdownGracefully();
        }
    }
}
