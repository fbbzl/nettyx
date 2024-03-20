package codec;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.lang.Console;
import codec.model.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.serializer.struct.StructSerializer;
import org.fz.nettyx.serializer.struct.TypeRefer;
import org.fz.nettyx.serializer.struct.basic.c.signed.Clong4;
import org.fz.nettyx.serializer.struct.basic.c.unsigned.Culong8;

import java.util.concurrent.TimeUnit;

@Slf4j
public class UserCodec extends SimpleChannelInboundHandler<ByteBuf> {

    TypeRefer<User<Clong4, Wife<Culong8, Son<Clong4, Bill>>, GirlFriend>> typeRefer = new TypeRefer<User<Clong4,
            Wife<Culong8, Son<Clong4, Bill>>, GirlFriend>>() {
    };

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
        StopWatch s = new StopWatch();
        s.start("read");
        User<Clong4, Wife<Culong8, Son<Clong4, Bill>>, GirlFriend> read = StructSerializer.read(typeRefer, msg);
        s.stop();

        Console.print(s.prettyPrint(TimeUnit.MILLISECONDS));

    }
}