package org.fz.nettyx.handler;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import lombok.RequiredArgsConstructor;
import org.fz.nettyx.function.ChannelExceptionAction;

/**
 * keep this handler in the codding-top
 *
 * @author fengbinbin
 * @since 2022-02-02 19:38
 **/

@RequiredArgsConstructor
class OutBoundExceptionHandler extends ChannelOutboundHandlerAdapter {

    private static final String DEFAULT_HANDLER_NAME = "$_exception_$";

    private final ChannelHandlerContext channelHandlerContext;
    private final ChannelExceptionAction exceptionCaughtAction;

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        promise.addListener((ChannelFutureListener) cfListener -> {
            if (!cfListener.isSuccess()) {
                exceptionCaughtAction.act(channelHandlerContext, cfListener.cause());
            }
        });
    }
}
