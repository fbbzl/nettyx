package client;

import static io.netty.buffer.Unpooled.wrappedBuffer;

import cn.hutool.core.lang.Console;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.codec.EscapeCodec;
import org.fz.nettyx.codec.EscapeCodec.EscapeMap;
import org.fz.nettyx.codec.StartEndFlagFrameCodec;
import org.fz.nettyx.handler.ChannelAdvice.InboundAdvice;
import org.fz.nettyx.handler.IdledHeartBeater.ReadIdleHeartBeater;
import org.fz.nettyx.handler.LoggerHandler;
import org.fz.nettyx.handler.LoggerHandler.Sl4jLevel;
import org.fz.nettyx.util.HexKit;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/3/1 22:59
 */

@Slf4j
public class TestChannelInitializer<C extends Channel> extends ChannelInitializer<C> {

    @Override
    protected void initChannel(C channel) {
        InboundAdvice inboundAdvice = new InboundAdvice(channel);
        inboundAdvice.whenExceptionCaught((ctx, t) -> Console.log(t));

        channel.pipeline().addLast(
            new ReadIdleHeartBeater(2, ctx -> {
                Console.log("心跳啦");
                ctx.channel().writeAndFlush(wrappedBuffer(HexKit.decode("7777")));
            })
            , new StartEndFlagFrameCodec(false, wrappedBuffer(new byte[]{(byte) 0x7e}))
            , new EscapeCodec(EscapeMap.mapHex("7e", "7d5e"))
           // , new UserCodec()
            , new LoggerHandler.InboundLogger(log, Sl4jLevel.ERROR)
            , inboundAdvice);
    }
}
