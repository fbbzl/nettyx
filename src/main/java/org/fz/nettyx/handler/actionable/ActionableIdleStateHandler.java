package org.fz.nettyx.handler.actionable;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.event.ChannelEvents;
import org.fz.nettyx.function.ChannelHandlerContextAction;

/**
 * @author fengbinbin
 * @since 2021-12-29 18:46
 **/
@Slf4j
@Getter
@Setter
@Accessors(fluent = true, chain = true)
public class ActionableIdleStateHandler extends IdleStateHandler {

    private ChannelHandlerContextAction readIdleAction, writeIdleAction, allIdleAction;
    private final boolean fireNext;

    @Override
    public boolean isSharable() {
        return true;
    }

    public long getReaderIdleSeconds() { return super.getReaderIdleTimeInMillis() / 1000; }
    public long getWriterIdleSeconds() { return super.getWriterIdleTimeInMillis() / 1000; }
    public long getAllIdleSeconds()    { return super.getAllIdleTimeInMillis()    / 1000; }

    public static ActionableIdleStateHandler newReadIdleHandler(int seconds, ChannelHandlerContextAction idleAction) {
        return new ActionableIdleStateHandler(seconds, 0, 0).readIdleAction(idleAction);
    }

    public static ActionableIdleStateHandler newWriteIdleHandler(int seconds, ChannelHandlerContextAction idleAction) {
        return new ActionableIdleStateHandler(0, seconds, 0).writeIdleAction(idleAction);
    }

    public static ActionableIdleStateHandler newAllIdleHandler(int seconds, ChannelHandlerContextAction idleAction) {
        return new ActionableIdleStateHandler(0, 0, seconds).allIdleAction(idleAction);
    }

    @Override
    protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) throws Exception {
        if (ChannelEvents.isReadIdle(evt)) {
            log.warn("have been in read-idle state for [{}] seconds on [{}]", getReaderIdleSeconds(), ctx.channel().remoteAddress());
            act(readIdleAction, ctx);
        }
        else
        if (ChannelEvents.isWriteIdle(evt)) {
            log.warn("have been in write-idle state for [{}] seconds on [{}]", getWriterIdleSeconds(), ctx.channel().remoteAddress());
            act(this.writeIdleAction(), ctx);
        }
        else
        if (ChannelEvents.isAllIdle(evt)) {
            log.warn("have been in all-idle state for [{}] seconds on [{}]", getAllIdleSeconds(), ctx.channel().remoteAddress());
            act(this.writeIdleAction(), ctx);
        }

        if (fireNext) super.channelIdle(ctx, evt);
    }

    static void act(ChannelHandlerContextAction channelAction, ChannelHandlerContext ctx) {
        if (channelAction != null) channelAction.act(ctx);
    }

    //********************************************        constructor start          **********************************************//

    public ActionableIdleStateHandler(int readerIdleTimeSeconds, int writerIdleTimeSeconds, int allIdleTimeSeconds) {
        this(readerIdleTimeSeconds, writerIdleTimeSeconds, allIdleTimeSeconds, true);
    }

    public ActionableIdleStateHandler(long readerIdleTime, long writerIdleTime, long allIdleTime, TimeUnit unit) {
        this(false, readerIdleTime, writerIdleTime, allIdleTime, unit, true);
    }

    public ActionableIdleStateHandler(int readerIdleTimeSeconds, int writerIdleTimeSeconds, int allIdleTimeSeconds, boolean fireNext) {
        this(readerIdleTimeSeconds, writerIdleTimeSeconds, allIdleTimeSeconds, TimeUnit.SECONDS, fireNext);
    }

    public ActionableIdleStateHandler(long readerIdleTime, long writerIdleTime, long allIdleTime, TimeUnit unit, boolean fireNext) {
        this(false, readerIdleTime, writerIdleTime, allIdleTime, unit, fireNext);
    }

    public ActionableIdleStateHandler(boolean observeOutput, long readerIdleTime, long writerIdleTime, long allIdleTime, TimeUnit unit) {
        this(observeOutput, readerIdleTime, writerIdleTime, allIdleTime, unit, true);
    }

    public ActionableIdleStateHandler(boolean observeOutput, long readerIdleTime, long writerIdleTime, long allIdleTime, TimeUnit unit, boolean fireNext) {
        super(observeOutput, readerIdleTime, writerIdleTime, allIdleTime, unit);
        this.fireNext = fireNext;
    }

    //********************************************        constructor end          **********************************************//

}
