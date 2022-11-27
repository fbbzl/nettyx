package org.fz.nettyx.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.AllArgsConstructor;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.event.ChannelEvents;
import org.fz.nettyx.function.ChannelHandlerContextAction;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2022/11/27 22:36
 */
@Slf4j
@UtilityClass
public class HeartBeater {
  @AllArgsConstructor
  public static class InboundHeartBeater extends ChannelInboundHandlerAdapter {

    private ChannelHandlerContextAction whenReadIdle;
    private int readIdleSeconds;

    public InboundHeartBeater(int readIdleSeconds, ChannelHandlerContextAction whenReadIdle) {
      this.readIdleSeconds = readIdleSeconds;
      this.whenReadIdle = whenReadIdle;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
      if (ChannelEvents.isReadIdle(evt)) {
        log.warn(
            "start inbound heartbeat on address [{}], next round will after [{}] seconds",
            ctx.channel().remoteAddress(),
            readIdleSeconds);
        act(whenReadIdle, ctx);
      }

      super.userEventTriggered(ctx, evt);
    }
  }

  @AllArgsConstructor
  public static class OutboundHeartBeater extends ChannelInboundHandlerAdapter {

    private ChannelHandlerContextAction whenWriteIdle;
    private int writesIdleSeconds;

    public OutboundHeartBeater(int writesIdleSeconds, ChannelHandlerContextAction whenWriteIdle) {
      this.writesIdleSeconds = writesIdleSeconds;
      this.whenWriteIdle = whenWriteIdle;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
      if (ChannelEvents.isWriteIdle(evt)) {
        log.warn(
            "start outbound heartbeat on address [{}], next round will after [{}] seconds",
            ctx.channel().remoteAddress(),
            writesIdleSeconds);
        act(whenWriteIdle, ctx);
      }

      super.userEventTriggered(ctx, evt);
    }
  }

  static void act(ChannelHandlerContextAction channelAction, ChannelHandlerContext ctx) {
    if (channelAction != null) {
      channelAction.act(ctx);
    }
  }
}
