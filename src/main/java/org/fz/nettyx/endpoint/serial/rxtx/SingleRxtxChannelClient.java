package org.fz.nettyx.endpoint.serial.rxtx;

import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;
import io.netty.channel.rxtx.RxtxDeviceAddress;
import io.netty.util.ReferenceCountUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * single channel rxtx client
 * @author fengbinbin
 * @since 2022-01-26 20:25
 **/

@Slf4j
@SuppressWarnings("deprecation")
public abstract class SingleRxtxChannelClient extends RxtxClient {

    protected Channel channel;

    protected void storeChannel(ChannelFuture cf) {
        storeChannel(cf.channel());
    }

    @SneakyThrows
    protected void storeChannel(Channel channel) {
        if (active(this.channel)) {
            this.channel.close().sync();
        }
        this.channel = channel;
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

    public abstract ChannelFuture connect(RxtxDeviceAddress address) throws Exception;

    public ChannelPromise send(Object message) {
        if (this.inActive(channel)) {
            log.debug("comm channel not in active status, message will be discard: {}", message);
            ReferenceCountUtil.safeRelease(message);
            return failurePromise(channel, "comm channel: [" + channel + "] is not usable");
        }

        try {
            if (unWritable(channel)) {
                log.debug("comm channel [{}] is not writable", channel);
                ReferenceCountUtil.safeRelease(message);
                return failurePromise(channel, "comm channel: [" + channel + "] is not writable");
            } else return (ChannelPromise) channel.writeAndFlush(message);
        } catch (Exception exception) {
            throw new ChannelException("exception occurred while sending the message [" + message + "], comm-port is [" + channel.remoteAddress() + "]", exception);
        }
    }

}
