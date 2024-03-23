package codec;

import static org.fz.nettyx.endpoint.client.AbstractMultiChannelClient.channelKey;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.lang.Console;
import codec.model.Bill;
import codec.model.GirlFriend;
import codec.model.Son;
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
import org.fz.nettyx.serializer.struct.basic.c.unsigned.Culong8;


@Slf4j
public class UserCodec extends SimpleChannelInboundHandler<ByteBuf> {

    TypeRefer<User<Clong4, Wife<Culong8, Son<Clong4, Bill>>, GirlFriend>> typeRefer = new TypeRefer<User<Clong4,
            Wife<Culong8, Son<Clong4, Bill>>, GirlFriend>>() {
    };

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
        StopWatch s = new StopWatch();
        s.start("read:" + channelKey(ctx));
        User<Clong4, Wife<Culong8, Son<Clong4, Bill>>, GirlFriend> read = StructSerializer.read(typeRefer, msg);
        s.stop();

        Console.print(s.prettyPrint(TimeUnit.MILLISECONDS));

    }
}