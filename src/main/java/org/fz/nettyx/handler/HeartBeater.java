package org.fz.nettyx.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.event.ChannelEvents;
import org.fz.nettyx.action.ChannelHandlerContextAction;
import org.fz.nettyx.handler.actionable.ActionableIdleStateHandler;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * The type Heart beater.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2022 /11/27 22:36
 */
@Slf4j
@UtilityClass
public class HeartBeater {

  /**
   * The type Inbound heart beater.
   */
  public static class InboundHeartBeater extends ActionableIdleStateHandler {

    private final ChannelHandlerContextAction readIdleHeartBeatAction;

    /**
     * Instantiates a new Inbound heart beater.
     *
     * @param readIdleSeconds         the read idle seconds
     * @param readIdleHeartBeatAction the read idle heart beat action
     */
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

        invokeAction(readIdleHeartBeatAction, ctx);
      }
      super.channelIdle(ctx, evt);
    }
  }

  /**
   * The type Outbound heart beater.
   */
  public static class OutboundHeartBeater extends ActionableIdleStateHandler {

    private final ChannelHandlerContextAction writeIdleHeartBeatAction;

    /**
     * Instantiates a new Outbound heart beater.
     *
     * @param writeIdleSeconds         the write idle seconds
     * @param writeIdleHeartBeatAction the write idle heart beat action
     */
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
        invokeAction(writeIdleHeartBeatAction, ctx);
      }
      super.channelIdle(ctx, evt);
    }
  }

  /**
   * Act.
   *
   * @param channelAction the channel action
   * @param ctx           the ctx
   */
  static void invokeAction(ChannelHandlerContextAction channelAction, ChannelHandlerContext ctx) {
    if (channelAction != null) {
      channelAction.act(ctx);
    }
  }
}
