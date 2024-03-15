package org.fz.nettyx.handler;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.nio.charset.StandardCharsets.UTF_16;
import static java.nio.charset.StandardCharsets.UTF_16BE;
import static java.nio.charset.StandardCharsets.UTF_16LE;
import static java.nio.charset.StandardCharsets.UTF_8;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.CombinedChannelDuplexHandler;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.function.Function;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OFF > FATAL > ERROR > WARN > INFO > DEBUG > TRACE > ALL
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /4/28 22:02
 */
public class LoggerHandler extends CombinedChannelDuplexHandler<LoggerHandler.InboundLogger,
        LoggerHandler.OutboundLogger> {

    private static final Sl4jLevel DEFAULT_LEVEL = Sl4jLevel.ERROR;

    /**
     * The TO_HEX.
     */
    public static final Function<Object, String> TO_HEX = msg -> ByteBufUtil.hexDump((ByteBuf) msg),
    /**
     * The To ascii.
     */
    TO_ASCII = msg -> ((ByteBuf) msg).toString(US_ASCII),
    /**
     * The To iso 8859 1.
     */
    TO_ISO_8859_1 = msg -> ((ByteBuf) msg).toString(ISO_8859_1),
    /**
     * The To utf 16 be.
     */
    TO_UTF_16BE = msg -> ((ByteBuf) msg).toString(UTF_16BE),
    /**
     * The To utf 16 le.
     */
    TO_UTF_16LE = msg -> ((ByteBuf) msg).toString(UTF_16LE),
    /**
     * The To utf 16.
     */
    TO_UTF_16 = msg -> ((ByteBuf) msg).toString(UTF_16),
    /**
     * The To utf 8.
     */
    TO_UTF8 = msg -> ((ByteBuf) msg).toString(UTF_8),
    /**
     * The To gbk.
     */
    TO_GBK = msg -> ((ByteBuf) msg).toString(Charset.forName("GBK")),
    /**
     * The To gb 2312.
     */
    TO_GB2312 = msg -> ((ByteBuf) msg).toString(Charset.forName("GB2312")),
    /**
     * The To string.
     */
    TO_STRING = Objects::toString;

    private static final Function<Object, String> DEFAULT_FORMATTER = TO_STRING;

    /**
     * Instantiates a new Logger handler.
     *
     * @param topic the topic
     */
    public LoggerHandler(String topic) {
        super(new InboundLogger(topic), new OutboundLogger(topic));
    }

    /**
     * Instantiates a new Logger handler.
     *
     * @param topic            the topic
     * @param messageFormatter the message formatter
     */
    public LoggerHandler(String topic, Function<Object, String> messageFormatter) {
        super(new InboundLogger(topic, messageFormatter), new OutboundLogger(topic, messageFormatter));
    }

    /**
     * Instantiates a new Logger handler.
     *
     * @param logger the logger
     */
    public LoggerHandler(Logger logger) {
        super(new InboundLogger(logger), new OutboundLogger(logger));
    }

    /**
     * Instantiates a new Logger handler.
     *
     * @param logger           the logger
     * @param messageFormatter the message formatter
     */
    public LoggerHandler(Logger logger, Function<Object, String> messageFormatter) {
        super(new InboundLogger(logger, messageFormatter), new OutboundLogger(logger, messageFormatter));
    }

    /**
     * The type Inbound logger.
     */
    @EqualsAndHashCode(callSuper = false)
    @Data
    public static class InboundLogger extends ChannelInboundHandlerAdapter {
        private final Logger                   logger;
        private final Function<Object, String> messageFormatter;
        // output the matching level log
        private final Sl4jLevel                level;

        /**
         * Instantiates a new Inbound logger.
         *
         * @param topic the topic
         */
        public InboundLogger(String topic) {
            this(LoggerFactory.getLogger(topic));
        }

        /**
         * Instantiates a new Inbound logger.
         *
         * @param topic            the topic
         * @param messageFormatter the message formatter
         */
        public InboundLogger(String topic, Function<Object, String> messageFormatter) {
            this(LoggerFactory.getLogger(topic), messageFormatter);
        }

        /**
         * Instantiates a new Inbound logger.
         *
         * @param topic the topic
         * @param level the level
         */
        public InboundLogger(String topic, Sl4jLevel level) {
            this(LoggerFactory.getLogger(topic), level);
        }

        /**
         * Instantiates a new Inbound logger.
         *
         * @param topic            the topic
         * @param messageFormatter the message formatter
         * @param level            the level
         */
        public InboundLogger(String topic, Function<Object, String> messageFormatter, Sl4jLevel level) {
            this(LoggerFactory.getLogger(topic), messageFormatter, level);
        }

        /**
         * Instantiates a new Inbound logger.
         *
         * @param logger the logger
         */
        public InboundLogger(Logger logger) {
            this(logger, DEFAULT_FORMATTER);
        }

        /**
         * Instantiates a new Inbound logger.
         *
         * @param logger the logger
         * @param level  the level
         */
        public InboundLogger(Logger logger, Sl4jLevel level) {
            this(logger, DEFAULT_FORMATTER, level);
        }

        /**
         * Instantiates a new Inbound logger.
         *
         * @param logger           the logger
         * @param messageFormatter the message formatter
         */
        public InboundLogger(Logger logger, Function<Object, String> messageFormatter) {
            this(logger, messageFormatter, DEFAULT_LEVEL);
        }

        /**
         * Instantiates a new Inbound logger.
         *
         * @param logger           the logger
         * @param messageFormatter the message formatter
         * @param level            the level
         */
        public InboundLogger(Logger logger, Function<Object, String> messageFormatter, Sl4jLevel level) {
            this.logger           = logger;
            this.messageFormatter = messageFormatter;
            this.level            = level;
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            final String  format        = "received from [{}], message: [{}]";
            SocketAddress socketAddress = ctx.channel().remoteAddress();

            switch (level) {
                case ERROR:
                    if (logger.isErrorEnabled()) logger.error(format, socketAddress, messageFormatter.apply(msg));
                    break;
                case WARN:
                    if (logger.isWarnEnabled()) logger.warn(format, socketAddress, messageFormatter.apply(msg));
                    break;
                case INFO:
                    if (logger.isInfoEnabled()) logger.info(format, socketAddress, messageFormatter.apply(msg));
                    break;
                case DEBUG:
                    if (logger.isDebugEnabled()) logger.debug(format, socketAddress, messageFormatter.apply(msg));
                    break;
                case TRACE:
                    if (logger.isTraceEnabled()) logger.trace(format, socketAddress, messageFormatter.apply(msg));
                    break;
                default: /* default is do nothing */
                    break;
            }

            super.channelRead(ctx, msg);
        }
    }

    /**
     * The type Outbound logger.
     */
    @EqualsAndHashCode(callSuper = false)
    @Data
    public static class OutboundLogger extends ChannelOutboundHandlerAdapter {

        private final Logger                   logger;
        private final Function<Object, String> messageFormatter;
        private final Sl4jLevel                level;

        /**
         * Instantiates a new Outbound logger.
         *
         * @param topic the topic
         */
        public OutboundLogger(String topic) {
            this(LoggerFactory.getLogger(topic));
        }

        /**
         * Instantiates a new Outbound logger.
         *
         * @param topic            the topic
         * @param messageFormatter the message formatter
         */
        public OutboundLogger(String topic, Function<Object, String> messageFormatter) {
            this(LoggerFactory.getLogger(topic), messageFormatter);
        }

        /**
         * Instantiates a new Outbound logger.
         *
         * @param topic the topic
         * @param level the level
         */
        public OutboundLogger(String topic, Sl4jLevel level) {
            this(LoggerFactory.getLogger(topic), level);
        }

        /**
         * Instantiates a new Outbound logger.
         *
         * @param topic            the topic
         * @param messageFormatter the message formatter
         * @param level            the level
         */
        public OutboundLogger(String topic, Function<Object, String> messageFormatter, Sl4jLevel level) {
            this(LoggerFactory.getLogger(topic), messageFormatter, level);
        }

        /**
         * Instantiates a new Outbound logger.
         *
         * @param logger the logger
         */
        public OutboundLogger(Logger logger) {
            this(logger, DEFAULT_FORMATTER);
        }

        /**
         * Instantiates a new Outbound logger.
         *
         * @param logger the logger
         * @param level  the level
         */
        public OutboundLogger(Logger logger, Sl4jLevel level) {
            this(logger, DEFAULT_FORMATTER, level);
        }

        /**
         * Instantiates a new Outbound logger.
         *
         * @param logger           the logger
         * @param messageFormatter the message formatter
         */
        public OutboundLogger(Logger logger, Function<Object, String> messageFormatter) {
            this(logger, messageFormatter, DEFAULT_LEVEL);
        }

        /**
         * Instantiates a new Outbound logger.
         *
         * @param logger           the logger
         * @param messageFormatter the message formatter
         * @param level            the level
         */
        public OutboundLogger(Logger logger, Function<Object, String> messageFormatter, Sl4jLevel level) {
            this.logger           = logger;
            this.messageFormatter = messageFormatter;
            this.level            = level;
        }

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            Channel       channel       = ctx.channel();
            final String  format        = "write to [{}], message is: [{}]";
            SocketAddress socketAddress = channel.remoteAddress();

            switch (level) {
                case ERROR:
                    if (logger.isErrorEnabled()) logger.error(format, socketAddress, messageFormatter.apply(msg));
                    break;
                case WARN:
                    if (logger.isWarnEnabled()) logger.warn(format, socketAddress, messageFormatter.apply(msg));
                    break;
                case INFO:
                    if (logger.isInfoEnabled()) logger.info(format, socketAddress, messageFormatter.apply(msg));
                    break;
                case DEBUG:
                    if (logger.isDebugEnabled()) logger.debug(format, socketAddress, messageFormatter.apply(msg));
                    break;
                case TRACE:
                    if (logger.isTraceEnabled()) logger.trace(format, socketAddress, messageFormatter.apply(msg));
                    break;
                default: /* default is do nothing */
                    break;
            }

            super.write(ctx, msg, promise);
        }
    }

    /**
     * The enum Sl 4 j level.
     */
    public enum Sl4jLevel {
        /**
         * Error sl 4 j level.
         */
        ERROR,
        /**
         * Warn sl 4 j level.
         */
        WARN,
        /**
         * Info sl 4 j level.
         */
        INFO,
        /**
         * Debug sl 4 j level.
         */
        DEBUG,
        /**
         * Trace sl 4 j level.
         */
        TRACE,
        ;
    }
}
