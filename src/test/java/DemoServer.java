import static java.nio.charset.StandardCharsets.UTF_8;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import org.fz.nettyx.codec.StringMessageCodec;
import org.fz.nettyx.server.Server;

/**
 * @author fengbinbin
 * @since 2022-02-08 20:32
 **/
public class DemoServer extends Server {

    @Override
    public ChannelFuture bind(SocketAddress socketAddress) {
        return newServerBootstrap().childHandler(childInitializer()).bind(socketAddress);
    }

    private ChannelInitializer<NioServerSocketChannel> childInitializer() {
        return new ChannelInitializer<NioServerSocketChannel>() {
            @Override
            protected void initChannel(NioServerSocketChannel ch) {
                ch.pipeline().addLast(new StringMessageCodec(UTF_8));
            }
        };
    }

    public static void main(String[] args) throws Exception {
        DemoServer demoServer = new DemoServer();
        try {
            demoServer.bind(new InetSocketAddress(8888)).channel().closeFuture().sync();
        } finally {
            demoServer.shutdownGracefully();
        }
    }
}
