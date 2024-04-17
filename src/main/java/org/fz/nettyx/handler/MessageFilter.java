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
public class MessageFilter extends ChannelDuplexHandler {

    public MessageFilter() {
        this.quiet = false;
    }

    public MessageFilter(boolean quiet, boolean stealable) {
        this.quiet = quiet;
    }

    public boolean stealable() {
        return stealable;
    }

    public static MessageFilter getStealer(Channel channel) {
        return getStealer(channel.pipeline());
    }

    public static MessageFilter getStealer(ChannelPipeline pipeline) {
        return pipeline.get(MessageFilter.class);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (stealable) {
            // import to release msg
            ReferenceCountUtil.release(msg);
            if (!quiet) {
                log.debug("has stolen inbound-message [{}]", msg, new InboundStolenException(msg));
            }
        }
        // else the msg will still goon
        else {
            super.channelRead(ctx, msg);
        }
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (stealable) {
            promise.setFailure(new OutboundStolenException(msg));

            // import to release msg
            ReferenceCountUtil.release(msg);
            if (!quiet) {
                log.debug("has stolen outbound-message [{}]", msg);
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
