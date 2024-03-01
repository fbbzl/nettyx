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
import org.fz.nettyx.handler.IdledHeartBeater.ReadIdleHeartBeater;
import org.fz.nettyx.handler.LoggerHandler;
import org.fz.nettyx.handler.LoggerHandler.Sl4jLevel;
import org.fz.nettyx.util.HexKit;

import java.net.InetSocketAddress;

import static io.netty.buffer.Unpooled.wrappedBuffer;
import static java.util.concurrent.TimeUnit.SECONDS;

@Slf4j
public class TestSingleTcp extends SingleTcpChannelClient {

    public static final ChannelInitializer<NioSocketChannel> CHANNEL_INITIALIZER =
            new ChannelInitializer<NioSocketChannel>() {
        @Override
        protected void initChannel(NioSocketChannel channel) {
            channel.pipeline().addLast(
                    new ReadIdleHeartBeater(2, ctx -> {
                        Console.log("心跳啦");
                        ctx.channel().writeAndFlush(wrappedBuffer(HexKit.decode("7777")));
                    })
                    , new StartEndFlagFrameCodec(false, wrappedBuffer(new byte[]{(byte) 0x7e}))
                    , new EscapeCodec(EscapeMap.mapHex("7e", "7d5e"))
                    , new UserCodec()
                    , new LoggerHandler.InboundLogger(log, Sl4jLevel.ERROR));
        }
    };

    protected TestSingleTcp(InetSocketAddress remoteAddress) {
        super(remoteAddress);
    }

    @Override
    protected ChannelFutureAction whenConnectFailure() {
        return cf -> cf.channel().eventLoop().schedule(this::connect, 2, SECONDS);
    }

    @Override
    protected ChannelInitializer<NioSocketChannel> channelInitializer() {
        return CHANNEL_INITIALIZER;
    }

    public static void main(String[] args) {
        TestSingleTcp testClient = new TestSingleTcp(new InetSocketAddress("127.0.0.1", 9081));
        testClient.connect();
    }
}