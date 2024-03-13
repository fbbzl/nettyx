package codec;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.lang.Console;
import codec.model.GirlFriend;
import codec.model.Son;
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

    TypeRefer<User<Son<Clong4, Clong4>, Wife, GirlFriend>> typeRefer = new TypeRefer<User<Son<Clong4, Clong4>, Wife,
            GirlFriend>>() {
    };

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
        StopWatch s = new StopWatch();
        s.start("read");
        User<Son<Clong4, Clong4>, Wife, GirlFriend> read = StructSerializer.read(msg, typeRefer);
        s.stop();

        Console.print(s.prettyPrint(TimeUnit.MILLISECONDS));

//        byte[] xx = new byte[1024];
//        Arrays.fill(xx, (byte)1);
//        ctx.channel().writeAndFlush(Unpooled.wrappedBuffer(xx));
    }
}