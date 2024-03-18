package client;

import codec.UserCodec;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.codec.EscapeCodec;
import org.fz.nettyx.codec.EscapeCodec.EscapeMap;
import org.fz.nettyx.codec.StartEndFlagFrameCodec;
import org.fz.nettyx.handler.ChannelAdvice.InboundAdvice;
import org.fz.nettyx.handler.ChannelAdvice.OutboundAdvice;
import org.fz.nettyx.handler.LoggerHandler;

import static io.netty.buffer.Unpooled.wrappedBuffer;
import static org.fz.nettyx.handler.LoggerHandler.Sl4jLevel.INFO;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/3/1 22:59
 */

@Slf4j
public class TestChannelInitializer<C extends Channel> extends ChannelInitializer<C> {

    @Override
    protected void initChannel(C channel) {
        InboundAdvice inboundAdvice = new InboundAdvice(channel)
                .whenExceptionCaught((c, t) -> System.err.println("in error: [" + t + "]"));
        OutboundAdvice outboundAdvice = new OutboundAdvice(channel)
                .whenExceptionCaught((c, t) -> System.err.println("out error: [" + t + "]"));

        channel.pipeline().addLast(
                outboundAdvice
                , new StartEndFlagFrameCodec(1024 * 256, true, wrappedBuffer(new byte[]{(byte) 0x7e}))
                , new EscapeCodec(EscapeMap.mapHex("7e", "7d5e"))
                , new UserCodec()
                , new LoggerHandler(log, INFO)
                , inboundAdvice);
    }
}
