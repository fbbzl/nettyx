package client;

import cn.hutool.core.lang.Console;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.fz.nettyx.endpoint.tcp.client.MultiTcpChannelClient;
import org.fz.nettyx.listener.ActionableChannelFutureListener;

import java.net.SocketAddress;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/2/29 14:58
 */
public class TestMultiTcp extends MultiTcpChannelClient<String> {

    @Override
    protected void connect(String key, SocketAddress address) {
        Console.log("connecting address [" + address.toString() + "]");
        ChannelFutureListener listener = new ActionableChannelFutureListener()
                .whenSuccess(cf -> System.err.println("ok"))
                .whenFailure(cf -> cf.channel().eventLoop().schedule(() -> connect(key, address), 2, SECONDS));

        new Bootstrap()
                .attr(key)
                .group(getEventLoopGroup())
                .channel(NioSocketChannel.class)
                .handler(channelInitializer(address))
                .connect(address)
                .addListener(listener);
    }


}
