package codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import model.GirlFriend;
import model.Son;
import model.User;
import model.Wife;
import org.fz.nettyx.serializer.struct.TypeRefer;
import org.fz.nettyx.serializer.struct.basic.c.signed.Clong4;

@Slf4j
public class UserCodec extends SimpleChannelInboundHandler<ByteBuf> {
    TypeRefer<User<Son<Clong4, Clong4>, Wife, GirlFriend>> typeRefer = new TypeRefer<User<Son<Clong4, Clong4>, Wife, GirlFriend>>() {
    };

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
        System.err.println(msg.readableBytes());
        msg.release();
    }
}