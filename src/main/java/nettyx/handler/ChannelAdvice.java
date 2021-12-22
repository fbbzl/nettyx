package nettyx.handler;

import static nettyx.Logs.debug;
import static nettyx.Logs.error;
import static nettyx.Logs.info;
import static nettyx.Logs.warn;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoop;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import nettyx.event.ChannelEvents;
import nettyx.function.ChannelExceptionAction;
import nettyx.function.ChannelHandlerContextAction;

/**
 * channel advice
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2020 /11/19/019 10:05
 */
@Slf4j
@Setter
@NoArgsConstructor
@Accessors(chain = true, fluent = true)
public class ChannelAdvice extends ChannelInboundHandlerAdapter {

    private static final String
        READ_IDLE_NAME  = "_readIdle_",
        WRITE_IDLE_NAME = "_writeIdle_";

    private Channel channel;

    private ChannelHandlerContextAction
        whenChannelRegister,
        whenChannelUnRegister,
        whenChannelActive,
        whenChannelInactive,
        whenWritabilityChanged,
        whenChannelReadComplete,
        whenReadIdle,
        whenWriteIdle;

    private ChannelExceptionAction whenExceptionCaught;

    /**
     * used with {@link ChannelAdvice#whenReadIdle}
     */
    private int readIdleSeconds;
    /**
     * used with {@link ChannelAdvice#whenWriteIdle}
     */
    private int writesIdleSeconds;

    public ChannelAdvice(Channel channel) {
        this.channel = channel;
    }

    public ChannelAdvice whenReadIdle(int idleSeconds, ChannelHandlerContextAction act) {
        this.whenReadIdle    = act;
        this.readIdleSeconds = idleSeconds;

        ChannelPipeline pipeline  = channel.pipeline();
        EventLoop       eventLoop = channel.eventLoop();

        IdleStateHandler readIdleHandler  = new IdleStateHandler(this.readIdleSeconds, 0, 0),
                         writeIdleHandler = (IdleStateHandler) pipeline.get(WRITE_IDLE_NAME);

        // if writeIdleHandler configured, readIdleHandler will set before writeIdleHandler
        if (writeIdleHandler != null) {
            pipeline.addBefore(eventLoop, WRITE_IDLE_NAME, READ_IDLE_NAME, readIdleHandler);
        }
        else {
            pipeline.addFirst(eventLoop, READ_IDLE_NAME, readIdleHandler);
        }

        return this;
    }

    public ChannelAdvice whenWriteIdle(int idleSeconds, ChannelHandlerContextAction act) {
        this.whenWriteIdle     = act;
        this.writesIdleSeconds = idleSeconds;

        ChannelPipeline pipeline  = channel.pipeline();
        EventLoop       eventLoop = channel.eventLoop();

        IdleStateHandler writeIdleHandler = new IdleStateHandler(0, this.writesIdleSeconds, 0),
                         readIdleHandler  = (IdleStateHandler) pipeline.get(READ_IDLE_NAME);

        // if readIdleHandler configured, writeIdleHandler will set after readIdleHandler
        if (readIdleHandler != null) {
            pipeline.addAfter(eventLoop, READ_IDLE_NAME, WRITE_IDLE_NAME, writeIdleHandler);
        }
        else {
            pipeline.addFirst(eventLoop, WRITE_IDLE_NAME, writeIdleHandler);
        }

        return this;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        debug(log, "channel registered, remote-address is [{}], local-address is [{}]", ctx.channel().remoteAddress(), ctx.channel().localAddress());

        act(whenChannelRegister, ctx);

        super.channelRegistered(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        debug(log, "channel unregistered, remote-address is [{}], local-address is [{}]", ctx.channel().remoteAddress(), ctx.channel().localAddress());


        act(whenChannelUnRegister, ctx);

        super.channelUnregistered(ctx);
    }

    /**
     * do not support channel read advice!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
     */

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        debug(log, "channel read complete, remote-address is [{}], local-address is [{}]", ctx.channel().remoteAddress(), ctx.channel().localAddress());


        act(whenChannelReadComplete, ctx);

        super.channelReadComplete(ctx);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        debug(log, "channel writability changed, remote-address is [{}], local-address is [{}]", ctx.channel().remoteAddress(), ctx.channel().localAddress());


        act(whenWritabilityChanged, ctx);

        super.channelWritabilityChanged(ctx);
    }

    /**
     * channel active action, will run channelActiveAction
     *
     * @param ctx ChannelHandlerContext
     * @throws Exception ex
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        info(log, "channel active event triggered, address is [{}]", ctx.channel().remoteAddress());


        act(whenChannelActive, ctx);

        super.channelActive(ctx);
    }

    /**
     * channel in-active action, will run channelInactiveAction
     *
     * @param ctx ChannelHandlerContext
     * @throws Exception ex
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        warn(log, "channel in-active event triggered, address is [{}]", ctx.channel().remoteAddress());


        act(whenChannelInactive, ctx);

        super.channelInactive(ctx);
    }

    /**
     * Unified inbound exception handling, if the exception inherits from ChannelHandlerException, specific methods will be called to achieve
     *
     * @param ctx ChannelHandlerContext
     * @param cause Throwable
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        error(log, "channel handler exception occurred: ", cause);


        act(whenExceptionCaught, ctx, cause);

        ctx.channel().close();
    }

    /**
     * channel event action execution
     *
     * @param ctx ChannelHandlerContext
     * @param evt channel event
     * @throws Exception ex
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (ChannelEvents.isReadIdle(evt)) {
            warn(log, "have been in read-idle state for [{}] seconds on [{}]", readIdleSeconds, ctx.channel().remoteAddress());

            act(whenReadIdle, ctx);
        }

        if (ChannelEvents.isWriteIdle(evt)) {
            warn(log, "have been in write-idle state for [{}] seconds on [{}]", writesIdleSeconds, ctx.channel().remoteAddress());

            act(whenWriteIdle, ctx);
        }

        super.userEventTriggered(ctx, evt);
    }

    //********************************************      private start      ***************************************************//

    private void act(ChannelHandlerContextAction channelAction, ChannelHandlerContext ctx) {
        if (channelAction != null) {
            channelAction.act(ctx);
        }
    }

    private void act(ChannelExceptionAction exceptionAction, ChannelHandlerContext ctx, Throwable throwable) {
        if (exceptionAction != null) {
            exceptionAction.act(ctx, throwable);
        }
    }

    //********************************************      private end        ***************************************************//

}
