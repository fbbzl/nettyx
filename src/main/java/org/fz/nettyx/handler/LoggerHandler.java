package org.fz.nettyx.handler;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.nio.charset.StandardCharsets.UTF_16;
import static java.nio.charset.StandardCharsets.UTF_16BE;
import static java.nio.charset.StandardCharsets.UTF_16LE;
import static java.nio.charset.StandardCharsets.UTF_8;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.*;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.function.Function;

/**
 * OFF > FATAL > ERROR > WARN > INFO > DEBUG > TRACE > ALL
 * @author fengbinbin
 * @version 1.0
 * @since 2023/4/28 22:02
 */

public class LoggerHandler extends CombinedChannelDuplexHandler<LoggerHandler.InboundLogger, LoggerHandler.OutboundLogger> {

    private static final Sl4jLevel DEFAULT_LEVEL = Sl4jLevel.INFO;

    public static final Function<Object, String>
        TO_HEX = msg -> ByteBufUtil.hexDump((ByteBuf) msg),
        TO_ASCII = msg -> ((ByteBuf) msg).toString(US_ASCII),
        TO_ISO_8859_1 = msg -> ((ByteBuf) msg).toString(ISO_8859_1),
        TO_UTF_16BE = msg -> ((ByteBuf) msg).toString(UTF_16BE),
        TO_UTF_16LE = msg -> ((ByteBuf) msg).toString(UTF_16LE),
        TO_UTF_16 = msg -> ((ByteBuf) msg).toString(UTF_16),
        TO_UTF8 = msg -> ((ByteBuf) msg).toString(UTF_8),
        TO_GBK = msg -> ((ByteBuf) msg).toString(Charset.forName("GBK")),
        TO_GB2312 = msg -> ((ByteBuf) msg).toString(Charset.forName("GB2312")),
        TO_STRING = Objects::toString;

    private static final Function<Object, String> DEFAULT_FORMATTER = TO_HEX;

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
        private final Sl4jLevel level;

        public InboundLogger(String topic) {
            this(LoggerFactory.getLogger(topic));
        }

        public InboundLogger(String topic, Function<Object, String> messageFormatter) {
            this(LoggerFactory.getLogger(topic), messageFormatter);
        }

        public InboundLogger(String topic, Sl4jLevel level) {
            this(LoggerFactory.getLogger(topic), level);
        }

        public InboundLogger(String topic, Function<Object, String> messageFormatter, Sl4jLevel level) {
            this(LoggerFactory.getLogger(topic), messageFormatter, level);
        }

        public InboundLogger(Logger logger) {
            this(logger, DEFAULT_FORMATTER);
        }

        public InboundLogger(Logger logger, Sl4jLevel level) {
            this(logger, DEFAULT_FORMATTER, level);
        }

        public InboundLogger(Logger logger, Function<Object, String> messageFormatter) {
            this(logger, messageFormatter, DEFAULT_LEVEL);
        }

        public InboundLogger(Logger logger, Function<Object, String> messageFormatter, Sl4jLevel level) {
            this.logger = logger;
            this.messageFormatter = messageFormatter;
            this.level = level;
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            final String format = "received from [{}], message: [{}]";
            SocketAddress socketAddress = ctx.channel().remoteAddress();

            switch (level) {
                case ERROR: if (logger.isErrorEnabled()) logger.error(format, socketAddress, messageFormatter.apply(msg)); break;
                case WARN:  if (logger.isWarnEnabled())  logger.warn(format,  socketAddress, messageFormatter.apply(msg)); break;
                case INFO:  if (logger.isInfoEnabled())  logger.info(format,  socketAddress, messageFormatter.apply(msg)); break;
                case DEBUG: if (logger.isDebugEnabled()) logger.debug(format, socketAddress, messageFormatter.apply(msg)); break;
                case TRACE: if (logger.isTraceEnabled()) logger.trace(format, socketAddress, messageFormatter.apply(msg)); break;
                default: /* default is do nothing */ break;
            }

            super.channelRead(ctx, msg);
        }
    }

    @EqualsAndHashCode(callSuper = false)
    @Data
    public static class OutboundLogger extends ChannelOutboundHandlerAdapter {

        private final Logger logger;
        private final Function<Object, String> messageFormatter;
        private final Sl4jLevel level;

        public OutboundLogger(String topic) {
            this(LoggerFactory.getLogger(topic));
        }

        public OutboundLogger(String topic, Function<Object, String> messageFormatter) {
            this(LoggerFactory.getLogger(topic), messageFormatter);
        }

        public OutboundLogger(String topic, Sl4jLevel level) {
            this(LoggerFactory.getLogger(topic), level);
        }

        public OutboundLogger(String topic, Function<Object, String> messageFormatter, Sl4jLevel level) {
            this(LoggerFactory.getLogger(topic), messageFormatter, level);
        }

        public OutboundLogger(Logger logger) {
            this(logger, DEFAULT_FORMATTER);
        }

        public OutboundLogger(Logger logger, Sl4jLevel level) {
            this(logger, DEFAULT_FORMATTER, level);
        }

        public OutboundLogger(Logger logger, Function<Object, String> messageFormatter) {
            this(logger, messageFormatter, DEFAULT_LEVEL);
        }

        public OutboundLogger(Logger logger, Function<Object, String> messageFormatter, Sl4jLevel level) {
            this.logger = logger;
            this.messageFormatter = messageFormatter;
            this.level = level;
        }

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            Channel channel = ctx.channel();
            final String format = "channel [{}] write to [{}], message is: [{}]";
            SocketAddress socketAddress = channel.remoteAddress();

            switch (level) {
                case ERROR: if (logger.isErrorEnabled()) logger.error(format, channel, socketAddress, messageFormatter.apply(msg)); break;
                case WARN:  if (logger.isWarnEnabled())  logger.warn(format,  channel, socketAddress, messageFormatter.apply(msg)); break;
                case INFO:  if (logger.isInfoEnabled())  logger.info(format,  channel, socketAddress, messageFormatter.apply(msg)); break;
                case DEBUG: if (logger.isDebugEnabled()) logger.debug(format, channel, socketAddress, messageFormatter.apply(msg)); break;
                case TRACE: if (logger.isTraceEnabled()) logger.trace(format, channel, socketAddress, messageFormatter.apply(msg)); break;
                default: /* default is do nothing */ break;
            }

            super.write(ctx, msg, promise);
        }
    }

    public enum Sl4jLevel {
        ERROR, WARN, INFO, DEBUG, TRACE,
        ;
    }
}
