package org.fz.nettyx.template;

import com.google.common.reflect.TypeToken;
import io.netty.channel.*;
import lombok.Getter;

import java.lang.reflect.Type;

/**
 * top level template based on netty
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2021 /5/6 16:49
 */
@Getter
@SuppressWarnings("unchecked")
abstract class Template<C extends Channel> {

    private final        Class<C>       channelClass;
    private final        EventLoopGroup eventLoopGroup;

    protected Template()
    {
        this.eventLoopGroup = this.newEventLoopGroup();
        this.channelClass   = this.findChannelClass();
    }

    public static boolean gracefullyCloseable(Channel channel)
    {
        return channel != null && channel.isActive();
    }

    protected Class<C> findChannelClass()
    {
        Type channelType = TypeToken.of(this.getClass()).resolveType(Template.class.getTypeParameters()[0]).getType();
        if (channelType instanceof Class<?> channelClass && Channel.class.isAssignableFrom(channelClass)) {
            return (Class<C>) channelClass;
        }
        throw new IllegalStateException("can not resolve channel class for template [" + this.getClass() + "]");
    }

    protected abstract EventLoopGroup newEventLoopGroup();

    protected abstract ChannelInitializer<C> channelInitializer();

    protected boolean isRegistered(Channel channel)
    {
        return channel != null && channel.isRegistered();
    }

    protected boolean isOpen(Channel channel)
    {
        return channel != null && channel.isOpen();
    }

    protected boolean isActive(Channel channel)
    {
        return channel != null && channel.isActive();
    }

    protected boolean isWritable(Channel channel)
    {
        return channel != null && channel.isWritable();
    }

    protected boolean notRegistered(Channel channel)
    {
        return !isRegistered(channel);
    }

    protected boolean notOpen(Channel channel)
    {
        return !isOpen(channel);
    }

    protected boolean notActive(Channel channel)
    {
        return !isActive(channel);
    }

    protected boolean notWritable(Channel channel)
    {
        return !isWritable(channel);
    }

    protected ChannelPromise failurePromise(Channel channel)
    {
        return failurePromise(channel, "channel failure promise occur, channel: [" + channel + "]");
    }

    protected ChannelPromise failurePromise(Channel channel, String message)
    {
        if (channel == null) {
            throw new ChannelException(message);
        }
        return new DefaultChannelPromise(channel).setFailure(new ChannelException(message));
    }

    protected void shutdownGracefully()
    {
        getEventLoopGroup().shutdownGracefully();
    }

}
