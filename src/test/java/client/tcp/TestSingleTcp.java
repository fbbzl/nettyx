package client.tcp;


import client.TestChannelInitializer;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.endpoint.client.tcp.SingleTcpChannelClient;
import org.fz.nettyx.listener.ActionableChannelFutureListener;

@Slf4j
public class TestSingleTcp extends SingleTcpChannelClient {

    static ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    @Override
    protected ChannelInitializer<NioSocketChannel> channelInitializer() {
        return new TestChannelInitializer<>();
    }

    public static void main(String[] args) {
        TestSingleTcp testClient = new TestSingleTcp();

        ActionableChannelFutureListener listener = new ActionableChannelFutureListener()
            .whenSuccess(cf -> {
                executor.scheduleAtFixedRate(() -> {
                    byte[] msg = new byte[300];
                    Arrays.fill(msg, (byte) 1);
                    testClient.writeAndFlush(Unpooled.wrappedBuffer(msg));
                }, 2, 30, TimeUnit.MILLISECONDS);

                System.err.println(cf.channel().localAddress() + ": ok");
            })
            .whenCancel(cf -> System.err.println("cancel"))
            .whenFailure(cf -> {
                System.err.println(cf.channel().localAddress() + ": fail, " + cf.cause());
                cf.channel().eventLoop()
                  .schedule(() -> testClient.connect(cf.channel().remoteAddress()), 2, TimeUnit.SECONDS);
            })
            .whenDone(cf -> System.err.println("done"));

        testClient.connect(new InetSocketAddress("127.0.0.1", 9999)).addListener(listener);
    }
}