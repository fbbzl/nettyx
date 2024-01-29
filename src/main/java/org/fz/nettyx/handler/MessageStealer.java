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
public class MessageStealer extends ChannelDuplexHandler {

    /**
     * true: steal and release message with log
     * false: steal and release message without log
     */
    private final boolean quiet;

    private boolean stealable;

    /**
     * Instantiates a new Message stealer.
     */
    public MessageStealer() {
        this.quiet = false;
        this.stealable = false;
    }

    /**
     * Instantiates a new Message stealer.
     *
     * @param quiet     the quiet
     * @param stealable the stealable
     */
    public MessageStealer(boolean quiet, boolean stealable) {
        this.quiet = quiet;
        this.stealable = stealable;
    }

    /**
     * Stealable boolean.
     *
     * @return the boolean
     */
    public boolean stealable() {
        return stealable;
    }

    /**
     * End steal.
     */
    public void endSteal() {
        stealable = false;
    }

    /**
     * Start steal.
     */
    public void startSteal() {
        stealable = true;
    }

    /**
     * Gets stealer.
     *
     * @param channel the channel
     * @return the stealer
     */
    public static MessageStealer getStealer(Channel channel) {
        return getStealer(channel.pipeline());
    }

    /**
     * Gets stealer.
     *
     * @param pipeline the pipeline
     * @return the stealer
     */
    public static MessageStealer getStealer(ChannelPipeline pipeline) {
        return pipeline.get(MessageStealer.class);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (stealable) {
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

    /**
     * The type Inbound stolen exception.
     */
    public static class InboundStolenException extends RuntimeException {

        /**
         * Instantiates a new Inbound stolen exception.
         *
         * @param message the message
         */
        public InboundStolenException(Object message) {
            super("inbound message has been stolen [" + message + "]");
        }
    }

    /**
     * The type Outbound stolen exception.
     */
    public static class OutboundStolenException extends RuntimeException {

        /**
         * Instantiates a new Outbound stolen exception.
         *
         * @param message the message
         */
        public OutboundStolenException(Object message) {
            super("outbound message has been stolen [" + message + "]");
        }
    }
}
