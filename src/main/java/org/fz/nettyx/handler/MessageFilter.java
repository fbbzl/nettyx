package org.fz.nettyx.handler;

import io.netty.channel.*;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import lombok.RequiredArgsConstructor;

import java.util.function.Predicate;

/**
 * The type Message stealer.Can be used to filter parts of the message you don't need
 *
 * @author fengbinbin
 * @version 1.0
 * @since 4 /14/2022 7:30 PM
 */
@SuppressWarnings("unchecked")
public class MessageFilter extends ChannelHandlerAdapter {

    private static final InternalLogger log = InternalLoggerFactory.getInstance(MessageFilter.class);

    @RequiredArgsConstructor
    public static class InboundFilter<M> extends ChannelInboundHandlerAdapter {

        private final Predicate<M> fireCondition;

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (fireCondition.test((M) msg)) super.channelRead(ctx, msg);
            else {
                ReferenceCountUtil.release(msg);
                log.debug("has filter inbound-message [{}]", msg);
            }
        }
    }

    @RequiredArgsConstructor
    public static class OutboundFilter<M> extends ChannelOutboundHandlerAdapter {

        private final Predicate<M> fireCondition;

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            if (fireCondition.test((M) msg)) super.write(ctx, msg, promise);
            else {
                ReferenceCountUtil.release(msg);
                promise.setFailure(new UnsupportedOperationException("message has been filtered"));

                log.debug("has filter outbound-message [{}]", msg);
            }
        }
    }
}
