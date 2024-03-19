package codec;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.lang.Console;
import codec.model.User;
import codec.model.Wife;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.serializer.struct.StructSerializer;
import org.fz.nettyx.serializer.struct.TypeRefer;
import org.fz.nettyx.serializer.struct.basic.c.signed.Clong4;

import java.util.concurrent.TimeUnit;

@Slf4j
public class UserCodec extends SimpleChannelInboundHandler<ByteBuf> {

    TypeRefer<User<Clong4, Wife>> typeRefer = new TypeRefer<User<Clong4, Wife>>() {
    };

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
        StopWatch s = new StopWatch();
        s.start("read");
        User read = StructSerializer.read(msg, typeRefer);
        s.stop();
        System.err.println(read);
        Console.print(s.prettyPrint(TimeUnit.MILLISECONDS));

    }
}