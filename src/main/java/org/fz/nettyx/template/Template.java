package org.fz.nettyx.template;

import cn.hutool.core.util.TypeUtil;
import io.netty.channel.*;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import lombok.Getter;

import java.lang.reflect.Type;

/**
 * top level template based on netty
 *
 * @author fengbinbin
 * @since 2021 /5/6 16:49
 */
@Getter
@SuppressWarnings("unchecked")
abstract class Template<C extends Channel> {

    private static final InternalLogger log = InternalLoggerFactory.getInstance(Template.class);
    private final        Class<C>       channelClass;
    private final        EventLoopGroup eventLoopGroup;

    protected Template() {
        this.eventLoopGroup = newEventLoopGroup();
        this.channelClass   = this.findChannelClass();
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

    protected Class<C> findChannelClass() {
        Type     supperType;
        Class<?> supperClass = this.getClass();
        do {
            supperType  = supperClass.getGenericSuperclass();
            supperClass = supperClass.getSuperclass();
        } while (supperClass != Template.class);

        Type typeArgument = TypeUtil.getTypeArgument(supperType);

        Type[] actualTypes = TypeUtil.getActualTypes(this.getClass(), typeArgument);
        return (Class<C>) actualTypes[0];
    }

    protected abstract EventLoopGroup newEventLoopGroup();

    protected abstract ChannelInitializer<C> channelInitializer();

    protected boolean isRegistered(Channel channel) {
        return channel != null && channel.isRegistered();
    }

    protected boolean isOpen(Channel channel) {
        return channel != null && channel.isOpen();
    }

    protected boolean isActive(Channel channel) {
        return channel != null && channel.isActive();
    }

    protected boolean isWritable(Channel channel) {
        return channel != null && channel.isWritable();
    }

    protected boolean notRegistered(Channel channel) {
        return !isRegistered(channel);
    }

    protected boolean notOpen(Channel channel) {
        return !isOpen(channel);
    }

    protected boolean notActive(Channel channel) {
        return !isActive(channel);
    }

    protected boolean notWritable(Channel channel) {
        return !isWritable(channel);
    }

    protected ChannelPromise failurePromise(Channel channel) {
        return failurePromise(channel, "channel failure promise occur, channel: [" + channel + "]");
    }

    protected ChannelPromise failurePromise(Channel channel, String message) {
        return channel == null ? null : new DefaultChannelPromise(channel).setFailure(new ChannelException(message));
    }

    protected void shutdownGracefully() {
        getEventLoopGroup().shutdownGracefully();
    }

}
