package client;

import static io.netty.buffer.Unpooled.wrappedBuffer;

import codec.UserCodec;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.codec.EscapeCodec;
import org.fz.nettyx.codec.EscapeCodec.EscapeMap;
import org.fz.nettyx.codec.StartEndFlagFrameCodec;
import org.fz.nettyx.handler.LoggerHandler;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/3/1 22:59
 */

@Slf4j
public class TestChannelInitializer<C extends Channel> extends ChannelInitializer<C> {

    @Override
    protected void initChannel(C channel) {
        channel.pipeline().addLast(
            new StartEndFlagFrameCodec(true, wrappedBuffer(new byte[]{(byte) 0x7e}))
            , new EscapeCodec(EscapeMap.mapHex("7e", "7d5e"))
            , new UserCodec()
            , new LoggerHandler(log));
    }
}
