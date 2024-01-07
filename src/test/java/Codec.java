import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.codec.EscapeCodec;
import org.fz.nettyx.codec.StartEndFlagFrameCodec;
import org.fz.nettyx.endpoint.client.tcp.SingleTcpChannelClient;
import org.fz.nettyx.handler.AdvisableChannelInitializer;
import org.fz.nettyx.handler.LoggerHandler;
import org.fz.nettyx.handler.advice.InboundAdvice;

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
                            // ▼   ▲  deal control character and recover application data
                            .addLast(new LoggerHandler.InboundLogger(log));
                }
            };
        }
    }

    public static void main(String[] args) throws Exception {
        TestClient testClient = new TestClient();

        InetSocketAddress inetSocketAddress = new InetSocketAddress("127.0.0.1",8081);
        testClient.connect(inetSocketAddress).sync();

        System.err.println("ok");
    }


}
