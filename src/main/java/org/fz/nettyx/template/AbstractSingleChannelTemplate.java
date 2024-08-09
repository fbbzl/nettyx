package org.fz.nettyx.template;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import lombok.Getter;
import lombok.SneakyThrows;
import org.fz.nettyx.channel.ChannelState;
import org.fz.nettyx.listener.ActionChannelFutureListener;

import java.net.SocketAddress;

import static org.fz.nettyx.channel.ChannelState.CHANNEL_STATE_KEY;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/3/1 14:44
 */

@Getter
@SuppressWarnings({ "unchecked", "unused" })
public abstract class AbstractSingleChannelTemplate<C extends Channel, F extends ChannelConfig> extends Template<C> {

    private static final InternalLogger log = InternalLoggerFactory.getInstance(AbstractSingleChannelTemplate.class);
    private final        SocketAddress  remoteAddress;
    private final        Bootstrap      bootstrap;
    private              C              channel;

    protected AbstractSingleChannelTemplate(SocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
        this.bootstrap     = newBootstrap(remoteAddress);
    }

    public ChannelFuture connect() {
        ChannelFuture channelFuture = this.getBootstrap().clone().connect();
        channelFuture.addListeners(new ActionChannelFutureListener().whenSuccess((l, cf) -> this.storeChannel(cf)));

        return channelFuture;
    }

    protected void storeChannel(ChannelFuture cf) {
        storeChannel((C) cf.channel());
    }

    protected void storeChannel(C channel) {
        if (isActive(this.channel)) closeChannelDirectly(true);

        this.channel = channel;
    }

    @SneakyThrows(InterruptedException.class)
    public void closeChannelDirectly(boolean sync) {
        if (sync) this.channel.close().sync();
        else this.channel.close();
    }

    public void closeChannelGracefully() {
        if (gracefullyCloseable(channel)) {
            this.getChannel().close();
        }
    }

    public void closeChannelGracefully(ChannelPromise promise) {
        if (gracefullyCloseable(channel)) {
            this.getChannel().close(promise);
        }
    }

    public ChannelPromise write(Object message) {
        if (this.notActive(channel) || notWritable(channel)) {
            log.debug("channel not in usable status, message will be discard: {}", message);
            ReferenceCountUtil.safeRelease(message);
            return failurePromise(channel, "channel: [" + channel + "] is not usable");
        }

        try {
            return (ChannelPromise) channel.write(message);
        } catch (Exception exception) {
            throw new ChannelException("exception occurred while sending the message [" + message + "], address is ["
                                       + channel.remoteAddress() + "]", exception);
        }
    }

    public ChannelPromise writeAndFlush(Object message) {
        if (this.notActive(channel) || notWritable(channel)) {
            log.debug("channel not in usable status, message will be discard: {}", message);
            ReferenceCountUtil.safeRelease(message);
            return failurePromise(channel, "channel: [" + channel + "] is not usable");
        }

        try {
            return (ChannelPromise) channel.writeAndFlush(message);
        } catch (Exception exception) {
            throw new ChannelException("exception occurred while sending the message [" + message + "], address is ["
                                       + channel.remoteAddress() + "]", exception);
        }
    }

    protected Bootstrap newBootstrap(SocketAddress remoteAddress) {
        return new Bootstrap()
                .remoteAddress(remoteAddress)
                .group(getEventLoopGroup())
                .channelFactory(() -> {
                    C chl = new ReflectiveChannelFactory<>(getChannelClass()).newChannel();
                    doChannelConfig((F) chl.config());
                    return chl;
                })
                .attr(CHANNEL_STATE_KEY, new ChannelState())
                .handler(channelInitializer());
    }

    protected void doChannelConfig(F channelConfig) {
        // default is do nothing
    }

}
