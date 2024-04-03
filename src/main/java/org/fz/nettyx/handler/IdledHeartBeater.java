package org.fz.nettyx.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.action.ChannelHandlerContextAction;
import org.fz.nettyx.event.ChannelEvents;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.fz.nettyx.action.Actions.invokeAction;

/**
 * The type Heart beater.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2022 /11/27 22:36
 */
@Slf4j
@Getter
public abstract class IdledHeartBeater extends ActionIdleStateHandler {

    private final ChannelHandlerContextAction idledHeartBeatAction;

    protected IdledHeartBeater(int readIdleSeconds, ChannelHandlerContextAction readIdleHeartBeatAction) {
        super(readIdleSeconds, 0, 0, SECONDS);
        this.idledHeartBeatAction = readIdleHeartBeatAction;
    }

    abstract boolean condition(IdleStateEvent evt);

    abstract String stateName();

    @Override
    protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) throws Exception {
        if (condition(evt)) {
            log.warn(
                    "start [" + stateName() + "] heartbeat on address [{}], next round will after [{}] seconds",
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
        String stateName() {
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
        String stateName() {
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
        String stateName() {
            return "all-idle";
        }

        public AllIdleHeartBeater(int readIdleSeconds, ChannelHandlerContextAction readIdleHeartBeatAction) {
            super(readIdleSeconds, readIdleHeartBeatAction);
        }
    }

}
