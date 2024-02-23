import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.codec.EscapeCodec;
import org.fz.nettyx.codec.StartEndFlagFrameCodec;
import org.fz.nettyx.endpoint.tcp.client.SingleTcpChannelClient;
import org.fz.nettyx.handler.AdvisableChannelInitializer;
import org.fz.nettyx.handler.LoggerHandler;
import org.fz.nettyx.handler.advice.InboundAdvice;
import org.fz.nettyx.serializer.struct.StructSerializer;
import org.fz.nettyx.serializer.struct.StructSerializerContext;
import org.fz.nettyx.serializer.struct.TypeRefer;
import org.fz.nettyx.serializer.struct.basic.c.signed.Clong4;
import org.junit.Test;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2023/5/23 21:35
 */
@Slf4j
public class ClientTest {

    @Test
    public void initClient( ) throws Exception {
        StructSerializerContext structSerializerContext = new StructSerializerContext("org.fz.nettyx");

        TestClient testClient = new TestClient();

        InetSocketAddress inetSocketAddress = new InetSocketAddress("127.0.0.1", 9081);
        testClient.connect(inetSocketAddress).sync();

        System.err.println("ok");
    }

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
                            .addLast(new StartEndFlagFrameCodec(1024 * 1024, false, Unpooled.wrappedBuffer(new byte[]{(byte) 0x7e})))
                            .addLast(new EscapeCodec(EscapeCodec.EscapeMap.mapHex("7e", "7d5e")))
                            .addLast(new UserCodec())
                            // ▼   ▲  deal control character and recover application data
                            .addLast(new LoggerHandler.InboundLogger(log, LoggerHandler.Sl4jLevel.ERROR));
                }
            };
        }
    }

    public static class UserCodec extends SimpleChannelInboundHandler<ByteBuf> {
        TypeRefer<StructSerializerTest.User<StructSerializerTest.Son<Clong4, Clong4>, StructSerializerTest.Wife, StructSerializerTest.GirlFriend>> typeRefer = new TypeRefer<StructSerializerTest.User<StructSerializerTest.Son<Clong4, Clong4>, StructSerializerTest.Wife, StructSerializerTest.GirlFriend>>() {
        };

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {

            long l = System.currentTimeMillis();
            Object read = StructSerializer.read(msg, typeRefer);

            System.err.println(System.currentTimeMillis()-l);

            log.error("{}", read);
        }
    }

}
