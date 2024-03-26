package client.tcp;


import client.TestChannelInitializer;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.endpoint.client.tcp.SingleTcpChannelClient;
import org.fz.nettyx.listener.ActionChannelFutureListener;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class TestSingleTcp extends SingleTcpChannelClient {

    public TestSingleTcp(InetSocketAddress address) {
        super(address);
    }

    static ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    @Override
    protected ChannelInitializer<NioSocketChannel> channelInitializer() {
        return new TestChannelInitializer<>();
    }

    public static void main(String[] args) {
        TestSingleTcp testClient = new TestSingleTcp(new InetSocketAddress(9888));

        ChannelFutureListener listener = new ActionChannelFutureListener()
                .whenSuccess((ls, cf) -> {
                    executor.scheduleAtFixedRate(() -> {
                        byte[] msg = new byte[300];
                        Arrays.fill(msg, (byte) 1);
                        testClient.writeAndFlush(Unpooled.wrappedBuffer(msg));
                    }, 2, 30, TimeUnit.MILLISECONDS);

                    System.err.println(cf.channel().localAddress() + ": ok");
                })
                .whenCancel((ls, cf) -> System.err.println("cancel"))
                .whenFailure((ls, cf) -> {
                    System.err.println(cf.channel().localAddress() + ": fail, " + cf.cause());
                    cf.channel().eventLoop().schedule(() -> testClient.connect().addListener(ls), 2, TimeUnit.SECONDS);
                })
                .whenDone((ls, cf) -> System.err.println("done"));

        testClient.connect().addListener(listener);
    }
}