package codec;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.lang.Console;
import codec.model.GirlFriend;
import codec.model.User;
import codec.model.Wife;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.serializer.struct.StructSerializer;
import org.fz.nettyx.serializer.struct.TypeRefer;
import org.fz.nettyx.serializer.struct.basic.c.signed.Clong4;

@Slf4j
public class UserCodec extends SimpleChannelInboundHandler<ByteBuf> {

    TypeRefer<User<Clong4, Wife, GirlFriend>> typeRefer = new TypeRefer<User<Clong4, Wife, GirlFriend>>() {
    };

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
        StopWatch s = new StopWatch();
        s.start("read");
        User<Clong4, Wife, GirlFriend> read = StructSerializer.read(typeRefer, msg);
        s.stop();
        System.err.println(read);
        Console.print(s.prettyPrint(TimeUnit.MILLISECONDS));

    }
}