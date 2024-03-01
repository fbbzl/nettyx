package codec;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.lang.Console;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import model.GirlFriend;
import model.Son;
import model.User;
import model.Wife;
import org.fz.nettyx.serializer.struct.StructSerializer;
import org.fz.nettyx.serializer.struct.TypeRefer;
import org.fz.nettyx.serializer.struct.basic.c.signed.Clong4;

import java.util.concurrent.TimeUnit;

@Slf4j
public class UserCodec extends SimpleChannelInboundHandler<ByteBuf> {
    TypeRefer<User<Son<Clong4, Clong4>, Wife, GirlFriend>> typeRefer = new TypeRefer<User<Son<Clong4, Clong4>, Wife, GirlFriend>>() {
    };

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
        StopWatch s = new StopWatch();
//        s.start("xml");
//        Model model1 = XmlSerializerContext.findModel("school", "student");
//        Dict doc = XmlSerializer.read(msg.duplicate(), model1);
//        s.stop();

        s.start("read");
        User<Son<Clong4, Clong4>, Wife, GirlFriend> read = StructSerializer.read(msg, typeRefer);
        s.stop();

        s.start("write");
        ByteBuf write = StructSerializer.write(read, typeRefer);
        byte[] bytes = new byte[write.readableBytes()];
        write.readBytes(bytes);
        write.release();
        s.stop();

        Console.print(s.prettyPrint(TimeUnit.MILLISECONDS));
    }
}