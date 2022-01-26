package org.fz.nettyx.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;
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
        if (preCloseGracefully(channel)) {
            this.closeChannel();
        }
    }

    public void closeChannelGracefully(ChannelPromise promise) {
        if (preCloseGracefully(channel)) {
            this.closeChannel(promise);
        }
    }

    public abstract void connect(RxtxDeviceAddress address);

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
