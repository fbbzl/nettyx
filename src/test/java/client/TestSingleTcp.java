package client;

import cn.hutool.core.lang.Console;
import codec.UserCodec;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.codec.EscapeCodec;
import org.fz.nettyx.codec.EscapeCodec.EscapeMap;
import org.fz.nettyx.codec.StartEndFlagFrameCodec;
import org.fz.nettyx.endpoint.tcp.client.SingleTcpChannelClient;
import org.fz.nettyx.handler.ChannelAdvice.InboundAdvice;
import org.fz.nettyx.handler.ChannelAdvice.OutboundAdvice;
import org.fz.nettyx.handler.IdledHeartBeater.ReadIdleHeartBeater;
import org.fz.nettyx.handler.LoggerHandler;
import org.fz.nettyx.listener.ActionableChannelFutureListener;
import org.fz.nettyx.util.HexKit;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import static io.netty.buffer.Unpooled.wrappedBuffer;
import static java.util.concurrent.TimeUnit.SECONDS;

@Slf4j
public class TestSingleTcp extends SingleTcpChannelClient {
    public static final InetSocketAddress remoteAddress = new InetSocketAddress("127.0.0.1", 9081);

    public static void main(String[] args) {
        TestSingleTcp testClient = new TestSingleTcp();

        testClient.connect(remoteAddress);


    }

    @Override
    public void connect(SocketAddress address) {
        Console.log("connecting address [" + address.toString() + "]");
        ChannelFutureListener listener = new ActionableChannelFutureListener()
                .whenSuccess(cf -> System.err.println("ok"))
                .whenFailure(cf -> cf.channel().eventLoop().schedule(() -> connect(address), 2, SECONDS));

        new Bootstrap()
                .group(getEventLoopGroup())
                .channel(NioSocketChannel.class)
                .handler(channelInitializer(address))
                .connect(address)
                .addListener(listener);
    }

    private ChannelInitializer<NioSocketChannel> channelInitializer(SocketAddress address) {
        return new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel channel) {
                InboundAdvice inAdvice = new InboundAdvice(channel)
                        .whenReadIdle(2, ctx -> Console.log("读闲置啦"))
                        .whenReadTimeout(5, false, (ctx, th) -> Console.log("读超时"))
                        .whenChannelInactive(ctx -> {
                            Console.log("invoke your re-connect method here");
                            TestSingleTcp.this.connect(address);
                        })
                        .whenExceptionCaught((ctx, th) -> Console.log("入站异常, ", th));

                OutboundAdvice outAdvice = new OutboundAdvice(channel)
                        .whenDisconnect((ctx, promise) -> Console.log("invoke your disconnect method"))
                        .whenWriteIdle(4, ctx -> Console.log("写闲置"))
                        .whenWriteTimeout(2, false, (ctx, th) -> Console.log("写超时"))
                        .whenClose((ctx, pro) -> Console.log("close"));

                channel.pipeline().addLast(
                        outAdvice
                        , new ReadIdleHeartBeater(2, ctx -> {
                            Console.log("心跳啦");
                            ctx.channel().writeAndFlush(wrappedBuffer(HexKit.decode("7777")));
                        })
                        , new StartEndFlagFrameCodec(false, wrappedBuffer(new byte[]{(byte) 0x7e}))
                        , new EscapeCodec(EscapeMap.mapHex("7e", "7d5e"))
                        , new UserCodec()
                        , new LoggerHandler.InboundLogger(log, LoggerHandler.Sl4jLevel.ERROR)
                        , inAdvice);
            }
        };
    }
}