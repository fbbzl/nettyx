package template.tcp;


import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.endpoint.tcp.client.SingleTcpChannellClient;
import org.fz.nettyx.listener.ActionChannelFutureListener;
import template.TestChannelInitializer;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.fz.nettyx.listener.ActionChannelFutureListener.redo;

@Slf4j
public class TestSingleTcp extends SingleTcpChannellClient {

    static ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public TestSingleTcp(InetSocketAddress address) {
        super(address);
    }

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
                .whenFailure(redo(() -> {
                    log.error("redo");
                    return testClient.connect();
                }, 2, TimeUnit.MILLISECONDS))
                .whenDone((ls, cf) -> System.err.println("done"));

        testClient.connect().addListener(listener);
    }
}