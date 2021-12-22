package nettyx.codec;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import java.nio.charset.Charset;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2021/4/9 16:35
 */

@Slf4j
@RequiredArgsConstructor
public class StringMessageCodec extends MessageToMessageCodec<byte[], String> {

    private final Charset charset;

    @Override
    protected void encode(ChannelHandlerContext ctx, String msg, List<Object> out) {
        out.add(msg.getBytes(charset));
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, byte[] msg, List<Object> out) throws Exception {
        out.add(new String(msg, charset));
    }
}
