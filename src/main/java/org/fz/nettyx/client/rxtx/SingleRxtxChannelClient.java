package org.fz.nettyx.client.rxtx;

import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;
import io.netty.channel.DefaultChannelPromise;
import io.netty.channel.rxtx.RxtxDeviceAddress;
import lombok.extern.slf4j.Slf4j;

/**
 * @author fengbinbin
 * @since 2022-01-26 20:25
 **/

@Slf4j
public abstract class SingleRxtxChannelClient extends RxtxClient {

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

    public abstract void connect(RxtxDeviceAddress address);

    public ChannelPromise send(Object message) {
        if (this.inActive(channel)) {
            log.debug("comm channel not in active status, message will be discard: {}", message);
            return channel == null ? null : new DefaultChannelPromise(channel).setFailure(new ChannelException("channel: [" + channel + "] is not usable"));
        }

        try {
            return (ChannelPromise) channel.writeAndFlush(message);
        } catch (Exception exception) {
            throw new ChannelException("exception occurred while sending the message [" + message + "], comm-port is [" + channel.remoteAddress() + "]", exception);
        }
    }

}
