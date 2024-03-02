package org.fz.nettyx.handler;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.fz.nettyx.action.Actions.invokeAction;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.action.ChannelHandlerContextAction;
import org.fz.nettyx.event.ChannelEvents;
import org.fz.nettyx.handler.actionable.ActionableIdleStateHandler;

/**
 * The type Heart beater.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2022 /11/27 22:36
 */
@Slf4j
@Getter
public abstract class IdledHeartBeater extends ActionableIdleStateHandler {

    private final ChannelHandlerContextAction idledHeartBeatAction;

    protected IdledHeartBeater(int readIdleSeconds, ChannelHandlerContextAction readIdleHeartBeatAction) {
        super(readIdleSeconds, 0, 0, SECONDS);
        this.idledHeartBeatAction = readIdleHeartBeatAction;
    }

    abstract boolean condition(IdleStateEvent evt);

    abstract String state();

    @Override
    protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) throws Exception {
        if (condition(evt)) {
            log.warn(
                    "start [" + state() + "] heartbeat on address [{}], next round will after [{}] seconds",
                    ctx.channel().remoteAddress(),
                    getReaderIdleSeconds());

            invokeAction(idledHeartBeatAction, ctx);
        }
        super.channelIdle(ctx, evt);
    }

    /**
     * The type Inbound heart beater.
     */
    public static class ReadIdleHeartBeater extends IdledHeartBeater {
        @Override
        String state() {
            return "read-idle";
        }

        @Override
        boolean condition(IdleStateEvent evt) {
            return ChannelEvents.isReadIdle(evt);
        }

        public ReadIdleHeartBeater(int readIdleSeconds, ChannelHandlerContextAction readIdleHeartBeatAction) {
            super(readIdleSeconds, readIdleHeartBeatAction);
        }
    }

    public static class WriteIdleHeartBeater extends IdledHeartBeater {
        @Override
        boolean condition(IdleStateEvent evt) {
            return ChannelEvents.isWriteIdle(evt);
        }

        @Override
        String state() {
            return "write-idle";
        }

        public WriteIdleHeartBeater(int readIdleSeconds, ChannelHandlerContextAction readIdleHeartBeatAction) {
            super(readIdleSeconds, readIdleHeartBeatAction);
        }
    }

    public static class AllIdleHeartBeater extends IdledHeartBeater {
        @Override
        boolean condition(IdleStateEvent evt) {
            return ChannelEvents.isAllIdle(evt);
        }

        @Override
        String state() {
            return "all-idle";
        }

        public AllIdleHeartBeater(int readIdleSeconds, ChannelHandlerContextAction readIdleHeartBeatAction) {
            super(readIdleSeconds, readIdleHeartBeatAction);
        }
    }

}
