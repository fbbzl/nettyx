package codec;

import codec.model.GirlFriend;
import codec.model.Son;
import codec.model.User;
import codec.model.Wife;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.serializer.struct.TypeRefer;
import org.fz.nettyx.serializer.struct.basic.c.signed.Clong4;
import org.fz.nettyx.util.HexKit;

@Slf4j
public class UserCodec extends SimpleChannelInboundHandler<ByteBuf> {

    TypeRefer<User<Son<Clong4, Clong4>, Wife, GirlFriend>> typeRefer = new TypeRefer<User<Son<Clong4, Clong4>, Wife, GirlFriend>>() {
    };

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
        System.err.println(msg.readableBytes());

        if (msg.readableBytes() == 8) {
            ctx.channel().writeAndFlush(Unpooled.wrappedBuffer(HexKit.decode("ffffffff")));
        }
        if (msg.readableBytes() == 4) {
            ctx.channel().writeAndFlush(Unpooled.wrappedBuffer(HexKit.decode("ffffffffffffffff")));
        }

    }
}