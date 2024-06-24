package codec;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.lang.Console;
import cn.hutool.core.lang.reflect.MethodHandleUtil;
import cn.hutool.core.util.ReflectUtil;
import codec.model.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.serializer.struct.StructSerializer;
import org.fz.nettyx.serializer.struct.TypeRefer;
import org.fz.nettyx.serializer.struct.basic.c.signed.Clong4;
import org.fz.nettyx.serializer.struct.basic.c.unsigned.Cuchar;
import org.fz.nettyx.serializer.struct.basic.c.unsigned.Culong8;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Constructor;
import java.util.concurrent.TimeUnit;

import static org.fz.nettyx.template.AbstractMultiChannelTemplate.channelKey;


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
        Cuchar bid = read.getBill().getBid();
        Console.print(bid.getValue());
        Console.print(s.prettyPrint(TimeUnit.MILLISECONDS));
    }

    public static class X {
        private int a;
    }

    static Constructor<X> userConstructor   = ReflectUtil.getConstructor(X.class);
    static MethodHandle   constructorHandle = MethodHandleUtil.findConstructor(X.class);

    @SneakyThrows
    public static void main(String[] args) {
        StopWatch s = new
                StopWatch("create");
        s.start("new");
        for (int i = 0; i < 100_000_000; i++) {
            X x = new X();
        }
        s.stop();

        s.start("reflect");
        for (int i = 0; i < 100_000_000; i++) {
            X x = userConstructor.newInstance();
        }
        s.stop();

        s.start("methodhandle");
        for (int i = 0; i < 100_000_000; i++) {
            X x = (X) constructorHandle.invoke();
        }
        s.stop();

        Console.log(s.prettyPrint(TimeUnit.MILLISECONDS));
    }
}