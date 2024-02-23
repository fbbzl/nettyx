package client;

import codec.UserCodec;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.io.File;
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
import org.fz.nettyx.listener.ActionableChannelFutureListener;
import org.fz.nettyx.serializer.struct.StructSerializerContext;
import org.fz.nettyx.serializer.xml.XmlSerializerContext;

@Slf4j
public class TestClient extends SingleTcpChannelClient {

    public static void main(String[] args) {
        new StructSerializerContext("org.fz.nettyx");
        String u = "pc";
        File file = new File("C:\\Users\\" + u + "\\Desktop\\school.xml");
        new XmlSerializerContext(file);

        TestClient testClient = new TestClient();
        ActionableChannelFutureListener listener = new ActionableChannelFutureListener();
        listener.whenSuccess(cf -> System.err.println("ok"));
        InetSocketAddress inetSocketAddress = new InetSocketAddress("127.0.0.1", 9081);
        testClient.connect(inetSocketAddress).addListener(listener);

    }

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