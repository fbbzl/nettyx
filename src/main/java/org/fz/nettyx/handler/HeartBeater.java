package org.fz.nettyx.handler;

import static java.util.concurrent.TimeUnit.SECONDS;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.event.ChannelEvents;
import org.fz.nettyx.function.ChannelHandlerContextAction;
import org.fz.nettyx.handler.actionable.ActionableIdleStateHandler;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2022/11/27 22:36
 */
@Slf4j
@UtilityClass
public class HeartBeater {

  public static class InboundHeartBeater extends ActionableIdleStateHandler {

    private final ChannelHandlerContextAction readIdleHeartBeatAction;

    public InboundHeartBeater(int readIdleSeconds, ChannelHandlerContextAction readIdleHeartBeatAction) {
      super(readIdleSeconds, 0, 0, SECONDS);
      this.readIdleHeartBeatAction = readIdleHeartBeatAction;
    }

    @Override
    protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) throws Exception {
      if (ChannelEvents.isReadIdle(evt)) {
        log.warn(
            "start inbound heartbeat on address [{}], next round will after [{}] seconds",
            ctx.channel().remoteAddress(),
            getReaderIdleSeconds());

        act(readIdleHeartBeatAction, ctx);
      }
      super.channelIdle(ctx, evt);
    }
  }

  public static class OutboundHeartBeater extends ActionableIdleStateHandler {

    private final ChannelHandlerContextAction writeIdleHeartBeatAction;

    public OutboundHeartBeater(int writeIdleSeconds, ChannelHandlerContextAction writeIdleHeartBeatAction) {
      super(0, writeIdleSeconds, 0, SECONDS);
      this.writeIdleHeartBeatAction = writeIdleHeartBeatAction;
    }

    @Override
    protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) throws Exception {
      if (ChannelEvents.isWriteIdle(evt)) {
        log.warn(
            "start outbound heartbeat on address [{}], next round will after [{}] seconds",
            ctx.channel().remoteAddress(),
            getWriterIdleSeconds());
        act(writeIdleHeartBeatAction, ctx);
      }
      super.channelIdle(ctx, evt);
    }
  }

  static void act(ChannelHandlerContextAction channelAction, ChannelHandlerContext ctx) {
    if (channelAction != null) {
      channelAction.act(ctx);
    }
  }
}
