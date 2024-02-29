package org.fz.nettyx.endpoint.serial.rxtx;

import static org.fz.nettyx.action.ChannelFutureAction.NOTHING;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.rxtx.RxtxDeviceAddress;
import io.netty.channel.socket.oio.OioSocketChannel;
import io.netty.util.ReferenceCountUtil;
import java.net.SocketAddress;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.action.ChannelFutureAction;

/**
 * single channel rxtx client
 *
 * @author fengbinbin
 * @since 2022-01-26 20:25
 **/

@Slf4j
@Getter
@SuppressWarnings("deprecation")
public abstract class SingleRxtxChannelClient extends RxtxClient {

    private final SocketAddress remoteAddress;
    private final Bootstrap      bootstrap;
    private final EventLoopGroup eventLoopGroup;

    protected Channel channel;

    protected void storeChannel(ChannelFuture cf) {
        storeChannel(cf.channel());
    }

    @SneakyThrows
    protected void storeChannel(Channel channel) {
        if (isActive(this.channel)) {
            this.channel.close().sync();
        }
        this.channel = channel;
    }

    public void closeChannel() {
        this.channel.close();
    }

    public void closeChannel(ChannelPromise promise) {
        this.channel.close(promise);
    }

    public void closeChannelGracefully() {
        if (gracefullyCloseable(channel)) { this.closeChannel(); }
    }

    public void closeChannelGracefully(ChannelPromise promise) {
        if (gracefullyCloseable(channel)) { this.closeChannel(promise); }
    }

    public ChannelFuture connect(RxtxDeviceAddress address) {

    }

    public ChannelPromise writeAndFlush(Object message) {
        if (this.notActive(channel)) {
            log.debug("comm channel not in active status, message will be discard: {}", message);
            ReferenceCountUtil.safeRelease(message);
            return failurePromise(channel, "comm channel: [" + channel + "] is not usable");
        }

        try {
            if (notWritable(channel)) {
                log.debug("comm channel [{}] is not writable", channel);
                ReferenceCountUtil.safeRelease(message);
                return failurePromise(channel, "comm channel: [" + channel + "] is not writable");
            } else { return (ChannelPromise) channel.writeAndFlush(message); }
        }
        catch (Exception exception) {
            throw new ChannelException("exception occurred while sending the message [" + message + "], comm-port is ["
                                           + channel.remoteAddress() + "]", exception);
        }
    }

    //***********************************           override start           *****************************************//

    protected ChannelFutureAction whenConnectDone() {
        return NOTHING;
    }

    protected ChannelFutureAction whenConnectCancel() {
        return NOTHING;
    }

    protected ChannelFutureAction whenConnectSuccess() {
        return NOTHING;
    }

    protected ChannelFutureAction whenConnectFailure() {
        return NOTHING;
    }

    protected Bootstrap newBootstrap(SocketAddress remoteAddress) {
        return new Bootstrap()
            .remoteAddress(remoteAddress)
            .group(getEventLoopGroup())
            .channel(OioSocketChannel.class)
            .handler(channelInitializer());
    }

    protected abstract ChannelInitializer<? extends Channel> channelInitializer();

    //************************************           override end           ******************************************//

}
