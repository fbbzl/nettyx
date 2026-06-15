package org.fz.nettyx.template.tcp.client;


import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.fz.nettyx.listener.ActionChannelFutureListener;
import org.fz.nettyx.template.TestChannelInitializer;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import static org.fz.nettyx.action.ListenerAction.redo;
import static org.fz.nettyx.codec.UserCodec.TEST_USER;


public class TestSingleTcpClient extends SingleChannelClientTemplate {

    private static final InternalLogger log = InternalLoggerFactory.getInstance(TestSingleTcpClient.class);

    public TestSingleTcpClient(InetSocketAddress address) {
        super(address);
    }

    @Override
    protected ChannelInitializer<NioSocketChannel> channelInitializer() {
        return new TestChannelInitializer<>();
    }

    public static void main(String[] args) {
        TestSingleTcpClient testClient = new TestSingleTcpClient(new InetSocketAddress(9888));

        ChannelFutureListener listener = new ActionChannelFutureListener()
                .whenSuccess((ls, cf) -> {

                    testClient.writeAndFlush(TEST_USER);

                    log.info(cf.channel().localAddress() + ": ok");
                })
                .whenCancelled((ls, cf) -> log.info("cancel"))
                .whenFailure(redo(testClient::connect, 10, TimeUnit.SECONDS, 3, (l, c) -> System.err.println("最后次失败后执行")))
                .whenDone((ls, cf) -> log.info("done"));

        testClient.connect().addListener(listener);
    }
}
