package codec;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.StopWatch;
import cn.hutool.core.lang.Console;
import cn.hutool.core.lang.reflect.MethodHandleUtil;
import codec.model.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.serializer.struct.StructSerializer;
import org.fz.nettyx.serializer.struct.TypeRefer;
import org.fz.nettyx.serializer.struct.basic.c.signed.Clong4;
import org.fz.nettyx.serializer.struct.basic.c.unsigned.Cuchar;
import org.fz.nettyx.serializer.struct.basic.c.unsigned.Culong8;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import static java.lang.invoke.MethodType.methodType;
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

    @Data
    public static class X<A> {
        private int a;

        public void setA(int a) {
            this.a = a;
        }
    }

    @SneakyThrows
    public static void main(String[] args) {
        X x = new X();
        StopWatch s = new
                StopWatch("create");
        s.start("set");
        for (int i = 0; i < 100_000; i++) {
            x.setA(1);
        }
        s.stop();

        Method setter = BeanUtil.getPropertyDescriptor(X.class, "a").getWriteMethod();
        s.start("reflect");
        for (int i = 0; i < 100_000; i++) {
            MethodHandleUtil.invoke(x, setter, 1);
        }
        s.stop();

        s.start("methodhandle");
        MethodHandle methodhandle = MethodHandles.lookup().findVirtual(X.class, "setA", methodType(void.class, int.class));
        for (int i = 0; i < 100_000; i++) {
            methodhandle.invokeWithArguments(x,1);
        }
        s.stop();

        Console.log(s.prettyPrint(TimeUnit.MILLISECONDS));
    }
}