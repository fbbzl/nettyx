package org.fz.nettyx.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.action.ChannelHandlerContextAction;
import org.fz.nettyx.event.ChannelEvents;

import java.util.concurrent.TimeUnit;

import static org.fz.nettyx.action.Actions.invokeAction;

/**
 * The type Actionable idle state handler.
 *
 * @author fengbinbin
 * @since 2021 -12-29 18:46
 */
@Slf4j
@Getter
@Setter
@Accessors(fluent = true, chain = true)
public class ActionIdleStateHandler extends IdleStateHandler {

    private ChannelHandlerContextAction readIdleAction, writeIdleAction, allIdleAction;
    private final boolean fireNext;

    /**
     * Gets reader idle seconds.
     *
     * @return the reader idle seconds
     */
    public long getReaderIdleSeconds() { return super.getReaderIdleTimeInMillis() / 1000; }

    /**
     * Gets writer idle seconds.
     *
     * @return the writer idle seconds
     */
    public long getWriterIdleSeconds() { return super.getWriterIdleTimeInMillis() / 1000; }

    /**
     * Gets all idle seconds.
     *
     * @return the all idle seconds
     */
    public long getAllIdleSeconds()    { return super.getAllIdleTimeInMillis()    / 1000; }

    /**
     * New read idle handler actionable idle state handler.
     *
     * @param seconds    the seconds
     * @param idleAction the idle action
     * @return the actionable idle state handler
     */
    public static ActionIdleStateHandler newReadIdleHandler(int seconds, ChannelHandlerContextAction idleAction) {
        return new ActionIdleStateHandler(seconds, 0, 0).readIdleAction(idleAction);
    }

    /**
     * New write idle handler actionable idle state handler.
     *
     * @param seconds    the seconds
     * @param idleAction the idle action
     * @return the actionable idle state handler
     */
    public static ActionIdleStateHandler newWriteIdleHandler(int seconds, ChannelHandlerContextAction idleAction) {
        return new ActionIdleStateHandler(0, seconds, 0).writeIdleAction(idleAction);
    }

    /**
     * New all idle handler actionable idle state handler.
     *
     * @param seconds    the seconds
     * @param idleAction the idle action
     * @return the actionable idle state handler
     */
    public static ActionIdleStateHandler newAllIdleHandler(int seconds, ChannelHandlerContextAction idleAction) {
        return new ActionIdleStateHandler(0, 0, seconds).allIdleAction(idleAction);
    }

    @Override
    protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) throws Exception {
        if (ChannelEvents.isReadIdle(evt)) {
            log.warn("have been in read-idle state for [{}] seconds on [{}]", getReaderIdleSeconds(), ctx.channel().remoteAddress());
            invokeAction(readIdleAction, ctx);
        }
        else
        if (ChannelEvents.isWriteIdle(evt)) {
            log.warn("have been in write-idle state for [{}] seconds on [{}]", getWriterIdleSeconds(), ctx.channel().remoteAddress());
            invokeAction(writeIdleAction, ctx);
        }
        else
        if (ChannelEvents.isAllIdle(evt)) {
            log.warn("have been in all-idle state for [{}] seconds on [{}]", getAllIdleSeconds(), ctx.channel().remoteAddress());
            invokeAction(allIdleAction, ctx);
        }

        if (fireNext) super.channelIdle(ctx, evt);
    }

    //********************************************        constructor start          **********************************************//

    public ActionIdleStateHandler(int readerIdleTimeSeconds, int writerIdleTimeSeconds, int allIdleTimeSeconds) {
        this(readerIdleTimeSeconds, writerIdleTimeSeconds, allIdleTimeSeconds, true);
    }

    /**
     * Instantiates a new Actionable idle state handler.
     *
     * @param readerIdleTime the reader idle time
     * @param writerIdleTime the writer idle time
     * @param allIdleTime    the all idle time
     * @param unit           the unit
     */
    public ActionIdleStateHandler(long readerIdleTime, long writerIdleTime, long allIdleTime, TimeUnit unit) {
        this(false, readerIdleTime, writerIdleTime, allIdleTime, unit, true);
    }

    /**
     * Instantiates a new Actionable idle state handler.
     *
     * @param readerIdleTimeSeconds the reader idle time seconds
     * @param writerIdleTimeSeconds the writer idle time seconds
     * @param allIdleTimeSeconds    the all idle time seconds
     * @param fireNext              the fire next
     */
    public ActionIdleStateHandler(int readerIdleTimeSeconds, int writerIdleTimeSeconds, int allIdleTimeSeconds, boolean fireNext) {
        this(readerIdleTimeSeconds, writerIdleTimeSeconds, allIdleTimeSeconds, TimeUnit.SECONDS, fireNext);
    }

    /**
     * Instantiates a new Actionable idle state handler.
     *
     * @param readerIdleTime the reader idle time
     * @param writerIdleTime the writer idle time
     * @param allIdleTime    the all idle time
     * @param unit           the unit
     * @param fireNext       the fire next
     */
    public ActionIdleStateHandler(long readerIdleTime, long writerIdleTime, long allIdleTime, TimeUnit unit, boolean fireNext) {
        this(false, readerIdleTime, writerIdleTime, allIdleTime, unit, fireNext);
    }

    /**
     * Instantiates a new Actionable idle state handler.
     *
     * @param observeOutput  the observe output
     * @param readerIdleTime the reader idle time
     * @param writerIdleTime the writer idle time
     * @param allIdleTime    the all idle time
     * @param unit           the unit
     */
    public ActionIdleStateHandler(boolean observeOutput, long readerIdleTime, long writerIdleTime, long allIdleTime, TimeUnit unit) {
        this(observeOutput, readerIdleTime, writerIdleTime, allIdleTime, unit, true);
    }

    /**
     * Instantiates a new Actionable idle state handler.
     *
     * @param observeOutput  the observe output
     * @param readerIdleTime the reader idle time
     * @param writerIdleTime the writer idle time
     * @param allIdleTime    the all idle time
     * @param unit           the unit
     * @param fireNext       the fire next
     */
    public ActionIdleStateHandler(boolean observeOutput, long readerIdleTime, long writerIdleTime, long allIdleTime, TimeUnit unit, boolean fireNext) {
        super(observeOutput, readerIdleTime, writerIdleTime, allIdleTime, unit);
        this.fireNext = fireNext;
    }

    //********************************************        constructor end          **********************************************//

}
