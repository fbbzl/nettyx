package org.fz.nettyx.handler;


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

    public long getReaderIdleSeconds() { return super.getReaderIdleTimeInMillis() / 1000; }
    public long getWriterIdleSeconds() { return super.getWriterIdleTimeInMillis() / 1000; }
    public long getAllIdleSeconds()    { return super.getAllIdleTimeInMillis()    / 1000; }

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

        super.channelIdle(ctx, evt);
    }

    static void act(ChannelHandlerContextAction channelAction, ChannelHandlerContext ctx) {
        if (channelAction != null) channelAction.act(ctx);
    }

    @Override
    public boolean isSharable() {
        return true;
    }

    public ActionableIdleStateHandler(int readerIdleTimeSeconds, int writerIdleTimeSeconds, int allIdleTimeSeconds) {
        super(readerIdleTimeSeconds, writerIdleTimeSeconds, allIdleTimeSeconds);
    }

    public ActionableIdleStateHandler(long readerIdleTime, long writerIdleTime, long allIdleTime, TimeUnit unit) {
        super(readerIdleTime, writerIdleTime, allIdleTime, unit);
    }

    public ActionableIdleStateHandler(boolean observeOutput, long readerIdleTime, long writerIdleTime, long allIdleTime, TimeUnit unit) {
        super(observeOutput, readerIdleTime, writerIdleTime, allIdleTime, unit);
    }
}
