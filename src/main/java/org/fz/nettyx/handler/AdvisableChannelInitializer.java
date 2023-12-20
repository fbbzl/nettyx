package org.fz.nettyx.handler;

import io.netty.channel.*;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.Getter;
import org.fz.nettyx.handler.ExceptionHandler.InboundExceptionHandler;
import org.fz.nettyx.handler.ExceptionHandler.OutboundExceptionHandler;
import org.fz.nettyx.handler.actionable.ActionableIdleStateHandler;

import java.net.SocketAddress;
import org.fz.nettyx.handler.advice.InboundAdvice;
import org.fz.nettyx.handler.advice.OutboundAdvice;

/**
 * The type Advisable channel initializer.
 *
 * @param <C> the type parameter
 * @author fengbinbin
 * @version 1.0
 * @since 2 /10/2022 1:24 PM
 */
@Getter
public abstract class AdvisableChannelInitializer<C extends Channel> extends ChannelInitializer<C> {

    /**
     * The Inbound advice.
     */
    static final String
        INBOUND_ADVICE     = "$_inboundAdvice_$",
    /**
     * The Outbound advice.
     */
    OUTBOUND_ADVICE    = "$_outboundAdvice_$",
    /**
     * The Read idle.
     */
    READ_IDLE          = "$_readIdle_$",
    /**
     * The Write idle.
     */
    WRITE_IDLE         = "$_writeIdle_$",
    /**
     * The Read time out.
     */
    READ_TIME_OUT      = "$_readTimeout_$",
    /**
     * The Write time out.
     */
    WRITE_TIME_OUT     = "$_writeTimeout_$",
    /**
     * The Inbound exception.
     */
    INBOUND_EXCEPTION  = "$_inboundExceptionHandler_$",
    /**
     * The Outbound exception.
     */
    OUTBOUND_EXCEPTION = "$_outboundExceptionHandler_$";

    private final InboundAdvice inboundAdvice;
    private final OutboundAdvice outboundAdvice;
    private final InboundExceptionHandler    inboundExceptionHandler  = new InboundExceptionHandler();
    private final OutboundExceptionHandler   outboundExceptionHandler = new OutboundExceptionHandler();
    private ActionableIdleStateHandler readIdleStateHandler, writeIdleStateHandler;
    private       ReadTimeoutHandler         readTimeoutHandler;
    private       WriteTimeoutHandler        writeTimeoutHandler;

    /**
     * Instantiates a new Advisable channel initializer.
     *
     * @param inboundAdvice the inbound advice
     */
    protected AdvisableChannelInitializer(InboundAdvice inboundAdvice) {
        this(inboundAdvice, null);
    }

    /**
     * Instantiates a new Advisable channel initializer.
     *
     * @param outboundAdvice the outbound advice
     */
    protected AdvisableChannelInitializer(OutboundAdvice outboundAdvice) {
        this(null, outboundAdvice);
    }

    /**
     * Instantiates a new Advisable channel initializer.
     *
     * @param inboundAdvice  the inbound advice
     * @param outboundAdvice the outbound advice
     */
    protected AdvisableChannelInitializer(InboundAdvice inboundAdvice, OutboundAdvice outboundAdvice) {
        this.inboundAdvice  = inboundAdvice;
        this.outboundAdvice = outboundAdvice;

        this.initInternalHandlers();
    }

    private void initInternalHandlers() {
        // init create after null check
        if (inboundAdvice != null) {
            this.readIdleStateHandler = inboundAdvice.readIdleStateHandler();
            this.readTimeoutHandler   = inboundAdvice.readTimeoutHandler();
            this.inboundExceptionHandler.whenExceptionCaught(inboundAdvice.whenExceptionCaught());
        }

        if (outboundAdvice != null) {
            this.writeIdleStateHandler = outboundAdvice.writeIdleStateHandler();
            this.writeTimeoutHandler   = outboundAdvice.writeTimeoutHandler();
            this.outboundExceptionHandler.whenExceptionCaught(outboundAdvice.whenExceptionCaught());
        }
    }

    /**
     * add custom handlers
     *
     * @param channel current channel
     */
    protected abstract void addHandlers(C channel);

    @Override
    protected void initChannel(C channel) {
        // if default channel-handler order do not satisfy you, please override this method
        this.addDefaultOrderedHandlers(channel);
    }

    /**
     * keep channel handler in such order as default :
     * <p>
     * 1. outboundExceptionHandler
     * 2. read-Idle
     * 3. read-timeout
     * 4. inboundAdvice
     * <p>
     * 5. [business channel-handlers]
     * <p>
     * 6. outboundAdvice
     * 7. write-timeout
     * 8. write-Idle
     * 9. inboundExceptionHandler
     *
     * @param channel the channel
     */
    void addDefaultOrderedHandlers(C channel) {
        ChannelPipeline pipeline = channel.pipeline();

        addNonNullLast(pipeline, OUTBOUND_EXCEPTION, outboundExceptionHandler);
        addNonNullLast(pipeline, READ_IDLE,          readIdleStateHandler);
        addNonNullLast(pipeline, READ_TIME_OUT,      readTimeoutHandler);
        addNonNullLast(pipeline, INBOUND_ADVICE,     inboundAdvice);
        this.addHandlers(channel);
        addNonNullLast(pipeline, OUTBOUND_ADVICE,    outboundAdvice);
        addNonNullLast(pipeline, WRITE_TIME_OUT,     writeTimeoutHandler);
        addNonNullLast(pipeline, WRITE_IDLE,         writeIdleStateHandler);
        addNonNullLast(pipeline, INBOUND_EXCEPTION,  inboundExceptionHandler);
    }

    /**
     * Add non null first.
     *
     * @param pipeline       the pipeline
     * @param name           the name
     * @param channelHandler the channel handler
     */
    static void addNonNullFirst(ChannelPipeline pipeline, String name, ChannelHandler channelHandler) {
        if (channelHandler != null) pipeline.addFirst(name, channelHandler);
    }

    /**
     * Add non null last.
     *
     * @param pipeline       the pipeline
     * @param name           the name
     * @param channelHandler the channel handler
     */
    static void addNonNullLast(ChannelPipeline pipeline, String name, ChannelHandler channelHandler) {
        if (channelHandler != null) pipeline.addLast(name, channelHandler);
    }

    /**
     * Add non null before.
     *
     * @param pipeline       the pipeline
     * @param targetName     the target name
     * @param name           the name
     * @param channelHandler the channel handler
     */
    static void addNonNullBefore(ChannelPipeline pipeline, String targetName, String name, ChannelHandler channelHandler) {
        if (channelHandler != null) pipeline.addBefore(targetName, name, channelHandler);
    }

    /**
     * Add non null after.
     *
     * @param pipeline       the pipeline
     * @param targetName     the target name
     * @param name           the name
     * @param channelHandler the channel handler
     */
    static void addNonNullAfter(ChannelPipeline pipeline, String targetName, String name, ChannelHandler channelHandler) {
        if (channelHandler != null) pipeline.addAfter(targetName, name, channelHandler);
    }

}
