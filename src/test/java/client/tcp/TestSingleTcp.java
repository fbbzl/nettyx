package client.tcp;

import cn.hutool.core.lang.Console;
import codec.UserCodec;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.action.ChannelFutureAction;
import org.fz.nettyx.codec.EscapeCodec;
import org.fz.nettyx.codec.EscapeCodec.EscapeMap;
import org.fz.nettyx.codec.StartEndFlagFrameCodec;
import org.fz.nettyx.endpoint.client.tcp.SingleTcpChannelClient;
import org.fz.nettyx.handler.ChannelAdvice.InboundAdvice;
import org.fz.nettyx.handler.ChannelAdvice.OutboundAdvice;
import org.fz.nettyx.handler.IdledHeartBeater.ReadIdleHeartBeater;
import org.fz.nettyx.handler.LoggerHandler;
import org.fz.nettyx.util.HexKit;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import static io.netty.buffer.Unpooled.wrappedBuffer;
import static java.util.concurrent.TimeUnit.SECONDS;

@Slf4j
public class TestSingleTcp extends SingleTcpChannelClient {

    protected TestSingleTcp(SocketAddress remoteAddress) {
        super(remoteAddress);
    }

    public static void main(String[] args) {
        TestSingleTcp testClient = new TestSingleTcp(new InetSocketAddress("127.0.0.1", 9081));
        testClient.connect();
    }

    @Override
    protected ChannelFutureAction whenConnectFailure() {
        return cf -> schedule(this::connect, 2, SECONDS);
    }

    @Override
    protected ChannelInitializer<NioSocketChannel> channelInitializer() {
        return new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel channel) {
                InboundAdvice inAdvice = new InboundAdvice(channel)
                        .whenReadIdle(2, ctx -> Console.log("读闲置啦"))
                        .whenReadTimeout(5, false, (ctx, th) -> Console.log("读超时"))
                        .whenChannelInactive(ctx -> {
                            Console.log("invoke your re-connect method here");
                            TestSingleTcp.this.connect();
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