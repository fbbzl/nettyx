package client.server;

import static io.netty.buffer.Unpooled.wrappedBuffer;

import client.TestChannelInitializer;
import codec.UserCodec;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.ServerSocketChannel;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.fz.nettyx.codec.EscapeCodec;
import org.fz.nettyx.codec.EscapeCodec.EscapeMap;
import org.fz.nettyx.codec.StartEndFlagFrameCodec;
import org.fz.nettyx.endpoint.server.TcpServer;
import org.fz.nettyx.handler.ChannelAdvice.InboundAdvice;
import org.fz.nettyx.handler.ChannelAdvice.OutboundAdvice;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/3/23 12:40
 */
public class TestServer extends TcpServer {

    static ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public TestServer(SocketAddress bindAddress) {
        super(bindAddress);
    }

    public TestServer(int bindPort) {
        super(bindPort);
    }

    @Override
    protected ChannelInitializer<ServerSocketChannel> childChannelInitializer() {
        return new TestChannelInitializer<ServerSocketChannel>() {
            @Override
            protected void initChannel(ServerSocketChannel channel) {
                InboundAdvice inboundAdvice = new InboundAdvice(channel)
                    .whenExceptionCaught((c, t) -> System.err.println("in error: [" + t + "]"));
                OutboundAdvice outboundAdvice = new OutboundAdvice(channel)
                    .whenExceptionCaught((c, t) -> System.err.println("out error: [" + t + "]"));

                channel.pipeline().addLast(
                    outboundAdvice
                    , new StartEndFlagFrameCodec(320, true, wrappedBuffer(new byte[]{(byte) 0x7e}))
                    , new EscapeCodec(EscapeMap.mapHex("7e", "7d5e"))
                    , new UserCodec()
                    , inboundAdvice);
            }

        };
    }

    public static void main(String[] args) {
        TestServer testServer = new TestServer(9999);
        Channel    channel    = testServer.bind().channel();
        channel.closeFuture().addListener(cf -> testServer.shutdownGracefully());
        executor.scheduleAtFixedRate(() -> {
            byte[] msg = new byte[300];
            Arrays.fill(msg, (byte) 1);
            channel.writeAndFlush(Unpooled.wrappedBuffer(msg));
        }, 2, 30, TimeUnit.MILLISECONDS);
    }
}
