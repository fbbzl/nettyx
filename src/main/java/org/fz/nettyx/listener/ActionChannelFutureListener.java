package org.fz.nettyx.listener;

import cn.hutool.core.lang.func.Func1;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.function.*;

import static cn.hutool.core.lang.func.LambdaUtil.getMethodName;

/**
 * The type Actionable channel future listener.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2021 /2/4 17:48
 */
@Slf4j
@Setter
@Accessors(chain = true, fluent = true)
public class ActionChannelFutureListener implements ChannelFutureListener {

    /**
     * When the different state of the channel is monitored, the corresponding method will be called
     */
    private ListenerAction
            whenSuccess,
            whenFailure,
            whenCancel,
            whenDone;

    @Override
    public void operationComplete(ChannelFuture channelFuture) {
        // success
        if (channelFuture.isSuccess()) invokeAction(whenSuccess, this, channelFuture);
        else
        // failed
        if (!channelFuture.isSuccess()) invokeAction(whenFailure, this, channelFuture);

        // done
        if (channelFuture.isDone()) invokeAction(whenDone, this, channelFuture);
        else
        // canceled
        if (channelFuture.isCancelled()) invokeAction(whenCancel, this, channelFuture);
    }

    /**
     * will re-execute the action after assigned delay and timeUnit
     */
    public static ListenerAction redo(Supplier<ChannelFuture> did, long delay, TimeUnit unit) {
        return (ls, cf) -> cf.channel().eventLoop().schedule(() -> did.get().addListener(ls), delay, unit);
    }

    public static ListenerAction redo(UnaryOperator<ChannelFuture> did, long delay, TimeUnit unit) {
        return (ls, cf) -> cf.channel().eventLoop().schedule(() -> did.apply(cf).addListener(ls), delay, unit);
    }

    public static ListenerAction redo(Func1<ChannelFuture, ChannelFuture> fn, long delay, TimeUnit unit, int times) {
        return (ls, cf) -> {
            Channel channel = cf.channel();
            SocketAddress remoteAddress = channel.remoteAddress();
            AttributeKey<Integer> key = AttributeKey.valueOf(remoteAddress + "_channel_redo_" + getMethodName(fn));

            Attribute<Integer> redoTimesAttr = channel.attr(key);
            System.err.println(redoTimesAttr.get());
            redoTimesAttr.setIfAbsent(times);
            Integer redoTimes = redoTimesAttr.get();
            if (redoTimes > 0) {
                System.err.println(redoTimes);
                channel.eventLoop().schedule(() -> fn.call(cf).addListener(ls), delay, unit);
                redoTimesAttr.set(--redoTimes);
            }
        };
    }

    ///TODO
    // 渐进
    public static ListenerAction redo(Func1<ChannelFuture, ChannelFuture> fn, long initialDelay, LongBinaryOperator step, long maxDelay, TimeUnit unit) {
        return (ls, cf) -> {
            Channel channel = cf.channel();
            SocketAddress remoteAddress = channel.remoteAddress();
            Attribute<Integer> redoTimesAttr = channel.attr(AttributeKey.valueOf(remoteAddress + "_channel_lbo_redo_" + getMethodName(fn)));

            log.info("redoing, remote-address is [{}]", channel.remoteAddress());
            channel.eventLoop().schedule(() -> fn.call(cf).addListener(ls), 1, unit);
        };
    }

    /////////////////////123123
    public interface ListenerAction {
        void act(ChannelFutureListener listener, ChannelFuture channelFuture);
    }

    public static void invokeAction(ListenerAction action, ChannelFutureListener listener, ChannelFuture cf) {
        if (action != null) {
            action.act(listener, cf);
        }
    }

}
