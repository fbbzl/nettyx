package org.fz.nettyx.endpoint.client;

import static org.fz.nettyx.action.ChannelFutureAction.NOTHING;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPromise;
import io.netty.channel.ReflectiveChannelFactory;
import io.netty.util.ReferenceCountUtil;
import java.net.SocketAddress;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.action.ChannelFutureAction;
import org.fz.nettyx.listener.ActionableChannelFutureListener;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/3/1 14:44
 */

@Slf4j
@Getter
@SuppressWarnings("unchecked")
public abstract class AbstractSingleChannelClient<C extends Channel> extends
                                                                     Client<C> {

    private final SocketAddress remoteAddress;
    private final Bootstrap     bootstrap;
    private       Channel       channel;

    protected AbstractSingleChannelClient(SocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
        this.bootstrap     = newBootstrap(remoteAddress);
    }

    public void connect() {
        ChannelFutureListener listener = new ActionableChannelFutureListener()
            .whenDone(whenConnectDone())
            .whenCancel(whenConnectCancel())
            .whenSuccess(cf -> {
                storeChannel(cf);
                whenConnectSuccess().act(cf);
            })
            .whenFailure(whenConnectFailure());

        this.getBootstrap()
            .clone()
            .connect()
            .addListener(listener);
    }

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

    public void closeChannelGracefully() {
        if (gracefullyCloseable(channel)) { this.getChannel().close(); }
    }

    public void closeChannelGracefully(ChannelPromise promise) {
        if (gracefullyCloseable(channel)) { this.getChannel().close(promise); }
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

    protected Bootstrap newBootstrap(SocketAddress remoteAddress) {
        return new Bootstrap()
            .remoteAddress(remoteAddress)
            .group(getEventLoopGroup())
            .channelFactory(() -> {
                C chl = new ReflectiveChannelFactory<>(getChannelClass()).newChannel();
                doChannelConfig(chl);
                return chl;
            })
            .handler(channelInitializer());
    }

    protected void doChannelConfig(C channel) {
        // default is nothing
    }

    protected abstract ChannelInitializer<? extends Channel> channelInitializer();

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

}
