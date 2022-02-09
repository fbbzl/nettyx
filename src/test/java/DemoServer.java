import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;
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
                ch.pipeline()
                    .addLast(new IdleStateHandler(5, 0, 0, TimeUnit.SECONDS))
                    .addLast(new ChannelInboundHandlerAdapter() {
                        @Override
                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                            System.err.println(msg);
                            super.channelRead(ctx, msg);
                        }

                        @Override
                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                            System.err.println("jihuo");
                            super.channelActive(ctx);
                        }
                        @Override
                        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                            System.out.println("客户端循环心跳监测发送: ");
                            if (evt instanceof IdleStateEvent) {
                                IdleStateEvent event = (IdleStateEvent) evt;
                                if (event.state() == IdleState.WRITER_IDLE) {
                                    ctx.writeAndFlush("biubiu");
                                }
                            }
                        }
                    });
            }
        };
    }

    public static void main(String[] args) throws Exception {
        DemoServer demoServer = new DemoServer();
        try {
            demoServer.bind(new InetSocketAddress(5655)).channel().closeFuture().sync();
        } finally {
            demoServer.shutdownGracefully();
        }
    }
}
