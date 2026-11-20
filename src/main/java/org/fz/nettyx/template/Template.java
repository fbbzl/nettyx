package org.fz.nettyx.template;

import com.google.common.reflect.TypeToken;
import io.netty.channel.*;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.logging.InternalLogger;
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

    static ChannelPromise send(Template<?> template, Channel channel, Object message, boolean flush,
                               boolean multiChannel, Object channelKey, InternalLogger logger)
    {
        if (template.notActive(channel) || template.notWritable(channel)) {
            if (multiChannel) {
                logger.debug("channel not in usable status, channel key is [{}], message will be discard: {}",
                             channelKey, message);
            } else {
                logger.debug("channel not in usable status, message will be discard: {}", message);
            }
            ReferenceCountUtil.safeRelease(message);
            return template.failurePromise(channel, "channel: [" + channel + "] is not usable");
        }

        ChannelPromise promise = channel.newPromise();
        try {
            if (flush) channel.writeAndFlush(message, promise);
            else       channel.write(message, promise);
            return promise;
        } catch (Exception exception) {
            ReferenceCountUtil.safeRelease(message);
            throw new ChannelException("exception occurred while sending the message [" + message + "], address is ["
                                       + channel.remoteAddress() + "]", exception);
        }
    }

    protected void shutdownGracefully()
    {
        getEventLoopGroup().shutdownGracefully();
    }

}
