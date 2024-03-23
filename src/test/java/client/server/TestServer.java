package client.server;

import static io.netty.buffer.Unpooled.wrappedBuffer;

import codec.UserCodec;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.ServerSocketChannel;
import java.net.SocketAddress;
import java.util.Arrays;
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

    public TestServer(SocketAddress bindAddress) {
        super(bindAddress);
    }

    public TestServer(int bindPort) {
        super(bindPort);
    }

    @Override
    protected ChannelInitializer<ServerSocketChannel> childChannelInitializer() {
        return new ChannelInitializer<ServerSocketChannel>() {
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
                    , new ChannelInboundHandlerAdapter() {
                        @Override
                        public void channelRead(ChannelHandlerContext ctx, Object inMsg) throws Exception {
                            byte[] msg = new byte[300];
                            Arrays.fill(msg, (byte) 1);
                            ctx.channel().writeAndFlush(Unpooled.wrappedBuffer(msg));
                            super.channelRead(ctx, inMsg);
                        }
                    }
                    , new UserCodec()
                    , inboundAdvice);
            }

        };
    }

    public static void main(String[] args) {
        TestServer testServer = new TestServer(9888);
        testServer.bind().channel().closeFuture().addListener(cf -> {
            System.err.println("关闭了");
            testServer.shutdownGracefully();
        });

    }
}
