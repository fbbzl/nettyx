package org.fz.nettyx.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.util.ReferenceCountUtil;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 4/14/2022 7:30 PM
 */

@Slf4j
public class MessageStealer extends ChannelDuplexHandler {

    private final boolean quiet;

    private final AtomicBoolean stealable;

    public MessageStealer() {
        this.quiet = false;
        this.stealable = new AtomicBoolean(false);
    }

    public MessageStealer(boolean quiet, boolean stealable) {
        this.quiet = quiet;
        this.stealable = new AtomicBoolean(stealable);
    }

    public boolean stealable() {
        return stealable.get();
    }

    public void endSteal() {
        stealable.set(true);
    }

    public void startSteal() {
        stealable.set(true);
    }

    public void opposite() {
        synchronized (this) {
            stealable.set(!stealable.get());
        }
    }

    public MessageStealer getStealer(Channel channel) {
        return getStealer(channel.pipeline());
    }

    public MessageStealer getStealer(ChannelPipeline pipeline) {
        return pipeline.get(MessageStealer.class);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (stealable.get()) {
            // import to release msg
            ReferenceCountUtil.release(msg);
            if (!quiet) {
                log.info("has stolen inbound-message [{}]", msg, new InboundStolenException(msg));
            }
        }
        // else the msg will still goon
        else {
            super.channelRead(ctx, msg);
        }
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (stealable.get()) {
            promise.setFailure(new OutboundStolenException(msg));

            // import to release msg
            ReferenceCountUtil.release(msg);
            if (!quiet) {
                log.info("has stolen outbound-message [{}]", msg);
            }
        }
        // else the msg will still goon
        else {
            super.write(ctx, msg, promise);
        }
    }

    public static class InboundStolenException extends RuntimeException {

        public InboundStolenException(Object message) {
            super("inbound message has been stolen [" + message + "]");
        }
    }

    public static class OutboundStolenException extends RuntimeException {

        public OutboundStolenException(Object message) {
            super("outbound message has been stolen [" + message + "]");
        }
    }
}
