import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.codec.EscapeCodec;
import org.fz.nettyx.codec.StartEndFlagFrameCodec;
import org.fz.nettyx.endpoint.tcp.client.SingleTcpChannelClient;
import org.fz.nettyx.handler.AdvisableChannelInitializer;
import org.fz.nettyx.handler.LoggerHandler;
import org.fz.nettyx.handler.advice.InboundAdvice;
import org.fz.nettyx.serializer.struct.StructSerializer;
import org.fz.nettyx.serializer.struct.TypeRefer;
import org.fz.nettyx.serializer.struct.basic.c.signed.Clong4;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2023/5/23 21:35
 */
public class Codec {

    @Slf4j
    public static class TestClient extends SingleTcpChannelClient {

        @SneakyThrows
        @Override
        public ChannelFuture connect(SocketAddress address) {
            return super.newBootstrap()
                    .handler(channelInitializer())
                    .connect(address);

        }

        private ChannelInitializer<NioSocketChannel> channelInitializer() {
            InboundAdvice inboundAdvice = new InboundAdvice();

            return new AdvisableChannelInitializer<NioSocketChannel>(inboundAdvice) {
                @Override
                protected void addHandlers(NioSocketChannel channel) {
                    channel.pipeline()
                            // in  out
                            // ▼   ▲  remove start and end flag
                            .addLast(new StartEndFlagFrameCodec(false, Unpooled.wrappedBuffer(new byte[]{(byte) 0x7e})))
                            .addLast(new EscapeCodec(EscapeCodec.EscapeMap.mapHex("5566", "7783")))
                            .addLast(new UserCodec())
                            // ▼   ▲  deal control character and recover application data
                            .addLast(new LoggerHandler.InboundLogger(log));
                }
            };
        }
    }

    public static class UserCodec extends ChannelInboundHandlerAdapter {
        TypeRefer<TypedSerializerTest.User<TypedSerializerTest.Son<Clong4, Clong4>, TypedSerializerTest.Wife, TypedSerializerTest.Wife, TypedSerializerTest.GirlFriend, TypedSerializerTest.Wife>> typeRefer = new TypeRefer<TypedSerializerTest.User<TypedSerializerTest.Son<Clong4, Clong4>, TypedSerializerTest.Wife, TypedSerializerTest.Wife, TypedSerializerTest.GirlFriend, TypedSerializerTest.Wife>>() {
        };

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            Object read = StructSerializer.read((ByteBuf) msg, typeRefer);
            System.err.println("user:" + read);

            super.channelRead(ctx, msg);
        }
    }

    public static void main(String[] args) throws Exception {
        TestClient testClient = new TestClient();

        InetSocketAddress inetSocketAddress = new InetSocketAddress("127.0.0.1", 9081);
        testClient.connect(inetSocketAddress).sync();

        System.err.println("ok");
    }


}
