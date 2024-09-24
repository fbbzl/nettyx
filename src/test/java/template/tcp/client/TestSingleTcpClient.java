package template.tcp.client;


import cn.hutool.core.lang.Console;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.fz.nettyx.listener.ActionChannelFutureListener;
import org.fz.nettyx.template.tcp.client.SingleTcpChannelClientTemplate;
import template.TestChannelInitializer;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import static codec.UserCodec.TEST_USER;
import static org.fz.nettyx.action.ListenerAction.redo;


public class TestSingleTcpClient extends SingleTcpChannelClientTemplate {

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

                    Console.log(cf.channel().localAddress() + ": ok");
                })
                .whenCancelled((ls, cf) -> Console.log("cancel"))
                .whenFailure(redo(testClient::connect, 10, TimeUnit.SECONDS, 3, (l, c) -> System.err.println("最后次失败后执行")))
                .whenDone((ls, cf) -> Console.log("done"));

        testClient.connect().addListener(listener);
    }
}