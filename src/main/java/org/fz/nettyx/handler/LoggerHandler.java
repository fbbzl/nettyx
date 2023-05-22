package org.fz.nettyx.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.function.Function;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2023/4/28 22:02
 */

public class LoggerHandler extends CombinedChannelDuplexHandler<LoggerHandler.InboundLogger, LoggerHandler.OutboundLogger> {

    private static final Function<Object, String> TO_HEX = msg -> ByteBufUtil.hexDump((ByteBuf) msg);
    private static final Function<Object, String> TO_STRING = Objects::toString;

    public LoggerHandler(String topic) {
        super(new InboundLogger(topic), new OutboundLogger(topic));
    }

    public LoggerHandler(String topic, Function<Object, String> messageFormatter) {
        super(new InboundLogger(topic, messageFormatter), new OutboundLogger(topic, messageFormatter));
    }

    public LoggerHandler(Logger logger) {
        super(new InboundLogger(logger), new OutboundLogger(logger));
    }

    public LoggerHandler(Logger logger, Function<Object, String> messageFormatter) {
        super(new InboundLogger(logger, messageFormatter), new OutboundLogger(logger, messageFormatter));
    }

    @EqualsAndHashCode(callSuper = false)
    @Data
    public static class InboundLogger extends ChannelInboundHandlerAdapter {
        private final Logger logger;
        private final Function<Object, String> messageFormatter;

        public InboundLogger(String topic) {
            this(LoggerFactory.getLogger(topic));
        }

        public InboundLogger(String topic, Function<Object, String> messageFormatter) {
            this(LoggerFactory.getLogger(topic), messageFormatter);
        }

        public InboundLogger(Logger logger) {
            this.logger = logger;
            this.messageFormatter = TO_HEX;
        }

        public InboundLogger(Logger logger, Function<Object, String> messageFormatter) {
            this.logger = logger;
            this.messageFormatter = messageFormatter;
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (logger.isInfoEnabled()) {
                Channel channel = ctx.channel();
                logger.info("channel [{}] received from [{}], message is: [{}]", channel, channel.remoteAddress(), messageFormatter.apply(msg));
            }
            super.channelRead(ctx, msg);
        }
    }

    @EqualsAndHashCode(callSuper = false)
    @Data
    public static class OutboundLogger extends ChannelOutboundHandlerAdapter {

        private final Logger logger;
        private final Function<Object, String> messageFormatter;

        public OutboundLogger(String topic) {
            this(LoggerFactory.getLogger(topic));
        }

        public OutboundLogger(String topic, Function<Object, String> messageFormatter) {
            this(LoggerFactory.getLogger(topic), messageFormatter);
        }

        public OutboundLogger(Logger logger) {
            this.logger = logger;
            this.messageFormatter = TO_HEX;
        }

        public OutboundLogger(Logger logger, Function<Object, String> messageFormatter) {
            this.logger = logger;
            this.messageFormatter = messageFormatter;
        }

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            if (logger.isInfoEnabled()) {
                Channel channel = ctx.channel();
                logger.info("channel [{}] write to [{}], message is: [{}]", channel, channel.remoteAddress(), messageFormatter.apply(msg));
            }

            super.write(ctx, msg, promise);
        }
    }
}
