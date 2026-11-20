package org.fz.nettyx.template;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import lombok.Getter;
import org.fz.nettyx.listener.ActionChannelFutureListener;
import org.fz.nettyx.util.ChannelState;

import java.net.SocketAddress;

import static org.fz.nettyx.util.ChannelState.CHANNEL_STATE_KEY;

/**
 * single channel template
 * @author fengbinbin
 * @version 1.0
 * @since 2024/3/1 14:44
 */

@Getter
@SuppressWarnings({ "unchecked", "unused" })
public abstract class AbstractSingleChannelTemplate<S extends SocketAddress, C extends Channel, F extends ChannelConfig> extends Template<C> {

    private static final InternalLogger log = InternalLoggerFactory.getInstance(AbstractSingleChannelTemplate.class);
    protected Bootstrap bootstrap;
    protected volatile C channel;
    protected final S remoteAddress;

    protected AbstractSingleChannelTemplate(S remoteAddress) {
        this.remoteAddress = remoteAddress;
        this.bootstrap = this.newBootstrap(remoteAddress);
    }

    public ChannelFuture connect()
    {
        ChannelFuture channelFuture = this.getBootstrap().clone().connect();
        channelFuture.addListeners(new ActionChannelFutureListener().whenSuccess((l, cf) -> this.storeChannel(cf)));

        return channelFuture;
    }

    protected void storeChannel(ChannelFuture cf)
    {
        storeChannel((C) cf.channel());
    }

    protected synchronized void storeChannel(C channel)
    {
        if (isActive(this.channel)) this.channel.close();

        this.channel = channel;
    }

    public void closeChannelDirectly()
    {
        if (channel != null) {
            this.channel.close();
        }
    }

    public void closeChannelGracefully()
    {
        if (gracefullyCloseable(channel)) {
            this.getChannel().close();
        }
    }

    public void closeChannelGracefully(ChannelPromise promise)
    {
        if (gracefullyCloseable(channel)) {
            this.getChannel().close(promise);
        }
    }

    public ChannelPromise write(Object message)
    {
        return Template.send(this, channel, message, false, false, null, log);
    }

    public ChannelPromise writeAndFlush(Object message)
    {
        return Template.send(this, channel, message, true, false, null, log);
    }

    protected Bootstrap newBootstrap(S remoteAddress)
    {
        return new Bootstrap()
                .remoteAddress(remoteAddress)
                .group(getEventLoopGroup())
                .channelFactory(() -> {
                    C chl = new ReflectiveChannelFactory<>(getChannelClass()).newChannel();
                    try {
                        doChannelConfig((F) chl.config());
                    } catch (RuntimeException | Error failure) {
                        chl.unsafe().closeForcibly();
                        throw failure;
                    }
                    return chl;
                })
                .attr(CHANNEL_STATE_KEY, new ChannelState())
                .handler(channelInitializer());
    }

    protected void doChannelConfig(F channelConfig) {
        // default is do nothing
    }

}
