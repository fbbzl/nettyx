package template.tcp.server;

import cn.hutool.core.lang.Console;
import codec.UserCodec;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.fz.nettyx.codec.EscapeCodec;
import org.fz.nettyx.codec.EscapeCodec.EscapeMap;
import org.fz.nettyx.codec.StartEndFlagFrameCodec;
import org.fz.nettyx.handler.ChannelAdvice.InboundAdvice;
import org.fz.nettyx.handler.ChannelAdvice.OutboundAdvice;
import org.fz.nettyx.handler.MessageEchoHandler;
import org.fz.nettyx.template.tcp.server.TcpServerTemplate;

import static io.netty.buffer.Unpooled.wrappedBuffer;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/3/23 12:40
 */
public class TestServer extends TcpServerTemplate {

    public TestServer(int bindPort) {
        super(bindPort);
    }

    @Override
    protected ChannelInitializer<NioSocketChannel> childChannelInitializer() {
        return new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel channel) {
                InboundAdvice inboundAdvice = new InboundAdvice(channel)
                        .whenExceptionCaught((c, t) -> Console.log("in error: [" + t + "]"));
                OutboundAdvice outboundAdvice = new OutboundAdvice(channel)
                        .whenExceptionCaught((c, t) -> Console.log("out error: [" + t + "]"));

                channel.pipeline().addLast(
                        outboundAdvice
                        , new StartEndFlagFrameCodec(3000, true, wrappedBuffer(new byte[]{(byte) 0x7e}))
                        , new EscapeCodec(EscapeMap.mapHex("7e", "7d5e"))
                        , new MessageEchoHandler()
                        , new UserCodec()
                        , inboundAdvice);
            }
        };
    }

    public static void main(String[] args) {
        TestServer    testServer = new TestServer(9888);
        ChannelFuture bindFuture = testServer.bind();
        bindFuture.addListener(cf -> Console.log("binding state:" + cf.isSuccess()));
        bindFuture.channel().closeFuture().addListener(cf -> {
            Console.log("关闭了");
            testServer.shutdownGracefully();
        });

    }
}
