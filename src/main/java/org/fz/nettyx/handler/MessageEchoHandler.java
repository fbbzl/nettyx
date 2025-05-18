package org.fz.nettyx.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * message echo handler, The received message will be resent to the remote end
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2024/7/26 16:10
 */
public class MessageEchoHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
    {
        Channel channel = ctx.channel();
        if (channel.isWritable()) channel.writeAndFlush(msg);
    }

}
