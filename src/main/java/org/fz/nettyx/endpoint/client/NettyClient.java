package org.fz.nettyx.endpoint.client;

import cn.hutool.core.util.TypeUtil;
import io.netty.channel.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.action.ChannelFutureAction;

import java.lang.reflect.Type;

import static org.fz.nettyx.action.ChannelFutureAction.NOTHING;

/**
 * top level client based on netty
 *
 * @since 2021 /5/6 16:49
 */
@Slf4j
@Getter
@SuppressWarnings("unchecked")
public abstract class NettyClient<C extends Channel> {
    private final Class<C>       channelClass;
    private final EventLoopGroup eventLoopGroup;

    protected NettyClient(EventLoopGroup eventLoopGroup) {
        this.eventLoopGroup = eventLoopGroup;
        this.channelClass   = this.findChannelClass();
    }

    protected Class<C> findChannelClass() {
        Type     supperType;
        Class<?> supperClass = this.getClass();
        do {
            supperType  = supperClass.getGenericSuperclass();
            supperClass = supperClass.getSuperclass();
        } while (supperClass != NettyClient.class);

        Type typeArgument = TypeUtil.getTypeArgument(supperType);

        Type[] actualTypes = TypeUtil.getActualTypes(this.getClass(), typeArgument);
        return (Class<C>) actualTypes[0];
    }

    protected abstract ChannelInitializer<? extends Channel> channelInitializer();

    protected boolean isRegistered(Channel channel)  { return channel != null && channel.isRegistered(); }

    protected boolean isOpen(Channel channel)        { return channel != null && channel.isOpen(); }

    protected boolean isActive(Channel channel)      { return channel != null && channel.isActive(); }

    protected boolean isWritable(Channel channel)    { return channel != null && channel.isWritable(); }

    protected boolean notRegistered(Channel channel) { return !isRegistered(channel); }

    protected boolean notOpen(Channel channel)       { return !isOpen(channel); }

    protected boolean notActive(Channel channel)     { return !isActive(channel); }

    protected boolean notWritable(Channel channel)   { return !isWritable(channel); }

    protected ChannelPromise failurePromise(Channel channel) {
        return failurePromise(channel, "channel failure promise occur, channel: [" + channel + "]");
    }

    protected ChannelPromise failurePromise(Channel channel, String message) {
        return channel == null ? null : new DefaultChannelPromise(channel).setFailure(new ChannelException(message));
    }

    protected void shutdownGracefully() {
        getEventLoopGroup().shutdownGracefully();
    }

    public static boolean gracefullyCloseable(Channel channel) {
        return
                channel != null
                &&
                !channel.isActive()
                &&
                !channel.isOpen()
                &&
                !channel.isWritable();
    }

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
