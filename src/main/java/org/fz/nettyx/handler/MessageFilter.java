package org.fz.nettyx.handler;

import io.netty.channel.*;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * The type Message stealer.Can be used to filter parts of the message you don't need
 *
 * @author fengbinbin
 * @version 1.0
 * @since 4 /14/2022 7:30 PM
 */
@Slf4j
@SuppressWarnings("unchecked")
public class MessageFilter extends ChannelHandlerAdapter {

    public static MessageFilter getStealer(Channel channel) {
        return getStealer(channel.pipeline());
    }

    public static MessageFilter getStealer(ChannelPipeline pipeline) {
        return pipeline.get(MessageFilter.class);
    }

    public static abstract class InboundFilter<M> extends ChannelInboundHandlerAdapter {

        public abstract boolean filterable(M msg);

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (filterable((M) msg)) {
                ReferenceCountUtil.release(msg);
                log.debug("has filter inbound-message [{}]", msg);
            } else super.channelRead(ctx, msg);
        }
    }

    public static abstract class OutboundFilter<M> extends ChannelOutboundHandlerAdapter {

        public abstract boolean filterable(M msg);

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            if (filterable((M) msg)) {
                ReferenceCountUtil.release(msg);
                promise.setFailure(new UnsupportedOperationException("message has been filtered"));

                log.debug("has filter outbound-message [{}]", msg);
            } else super.write(ctx, msg, promise);
        }
    }
}
