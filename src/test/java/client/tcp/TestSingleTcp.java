package client.tcp;


import static java.util.concurrent.TimeUnit.SECONDS;

import client.TestChannelInitializer;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.net.InetSocketAddress;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.action.ChannelFutureAction;
import org.fz.nettyx.endpoint.client.tcp.SingleTcpChannelClient;

@Slf4j
public class TestSingleTcp extends SingleTcpChannelClient {

    protected TestSingleTcp(InetSocketAddress remoteAddress) {
        super(remoteAddress);
    }

    @Override
    protected ChannelFutureAction whenConnectFailure() {
        return cf -> cf.channel().eventLoop().schedule(this::connect, 2, SECONDS);
    }

    @Override
    protected ChannelFutureAction whenConnectSuccess() {
        return cf -> {

            System.err.println(cf.channel().localAddress() + ": ok");
        };
    }

    @Override
    protected ChannelInitializer<NioSocketChannel> channelInitializer() {
        return new TestChannelInitializer<>();
    }

    public static void main(String[] args) {
        TestSingleTcp testClient = new TestSingleTcp(new InetSocketAddress("127.0.0.1", 9081));
        testClient.connect();
    }
}